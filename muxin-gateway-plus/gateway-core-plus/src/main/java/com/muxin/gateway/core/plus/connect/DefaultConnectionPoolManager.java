package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认连接池管理器实现
 * 统一管理所有协议的连接池，简化网关的连接管理
 * 
 * @author muxin
 */
@Slf4j
public class DefaultConnectionPoolManager implements ConnectionPoolManager {

    private final Map<Protocol, ConnectionFactory> connectionFactories = new ConcurrentHashMap<>();
    private final Map<Protocol, ConnectionPool> connectionPools = new ConcurrentHashMap<>();
    private final ConnectionPoolConfig config;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public DefaultConnectionPoolManager() {
        this(ConnectionPoolConfig.defaultConfig());
    }

    public DefaultConnectionPoolManager(ConnectionPoolConfig config) {
        this.config = config;
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPoolManager] 连接池管理器初始化");
            
            // todo 注册默认的HTTP连接工厂
        }
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPoolManager] 连接池管理器启动");
            
            for (ConnectionPool pool : connectionPools.values()) {
                pool.start();
            }
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPoolManager] 关闭连接池管理器");
            
            for (ConnectionPool pool : connectionPools.values()) {
                pool.shutdown();
            }
            
            for (ConnectionFactory factory : connectionFactories.values()) {
                try {
                    factory.shutdown().get();
                } catch (Exception e) {
                    log.error("关闭连接工厂异常", e);
                }
            }
            
            connectionPools.clear();
            connectionFactories.clear();
        }
    }

    @Override
    public ClientConnection getClientConnection(EndpointAddress target, Protocol protocol) {
        return getClientConnection(target, protocol, config.getConnectionTimeout());
    }

    @Override
    public ClientConnection getClientConnection(EndpointAddress target, Protocol protocol, Duration timeout) {
        if (isShutdown()) {
            throw new IllegalStateException("连接池管理器已关闭");
        }

        if (!supportsProtocol(protocol)) {
            throw new IllegalArgumentException("不支持的协议: " + protocol.type());
        }

        try {
            ConnectionPool pool = getOrCreateConnectionPool(protocol);
            Connection connection = pool.getConnection(target, protocol, timeout.toMillis());
            
            if (!(connection instanceof ClientConnection)) {
                throw new RuntimeException("获取的不是客户端连接");
            }
            
            return (ClientConnection) connection;
        } catch (Exception e) {
            throw new RuntimeException("获取客户端连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void returnConnection(Connection connection) {
        if (connection == null || isShutdown()) {
            return;
        }

        try {
            Protocol protocol = connection.getProtocol();
            ConnectionPool pool = connectionPools.get(protocol);
            
            if (pool != null) {
                pool.returnConnection(connection);
            } else {
                connection.close();
            }
        } catch (Exception e) {
            log.error("归还连接异常", e);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            Protocol protocol = connection.getProtocol();
            ConnectionPool pool = connectionPools.get(protocol);
            
            if (pool != null) {
                pool.releaseConnection(connection);
            } else {
                connection.close();
            }
        } catch (Exception e) {
            log.error("释放连接异常", e);
        }
    }

    @Override
    public void warmupPool(EndpointAddress target, Protocol protocol, int minConnections) {
        if (isShutdown() || !supportsProtocol(protocol)) {
            return;
        }

        try {
            ConnectionPool pool = getOrCreateConnectionPool(protocol);
            pool.warmup(target, protocol, minConnections);
        } catch (Exception e) {
            log.error("连接池预热失败", e);
        }
    }

    @Override
    public void removePool(EndpointAddress target, Protocol protocol) {
        // 简化实现：清理空闲连接
        if (!isShutdown()) {
            try {
                ConnectionPool pool = connectionPools.get(protocol);
                if (pool != null) {
                    pool.cleanupIdleConnections();
                }
            } catch (Exception e) {
                log.error("移除连接池异常", e);
            }
        }
    }

    @Override
    public void cleanupIdleConnections() {
        if (isShutdown()) {
            return;
        }

        for (ConnectionPool pool : connectionPools.values()) {
            try {
                pool.cleanupIdleConnections();
            } catch (Exception e) {
                log.error("清理空闲连接异常", e);
            }
        }
    }

    @Override
    public void cleanupUnhealthyPools() {
        cleanupIdleConnections();
    }

    @Override
    public int getPoolCount() {
        return connectionPools.size();
    }

    @Override
    public boolean supportsProtocol(Protocol protocol) {
        return connectionFactories.containsKey(protocol);
    }

    @Override
    public Set<Protocol> getSupportedProtocols() {
        return connectionFactories.keySet();
    }

    /**
     * 注册连接工厂
     */
    public void registerConnectionFactory(ConnectionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("连接工厂不能为空");
        }

        Protocol protocol = factory.getSupportedProtocol();
        connectionFactories.put(protocol, factory);
        
        log.info("注册连接工厂: {} -> {}", protocol.type(), factory.getClass().getSimpleName());
    }

    /**
     * 检查管理器是否已关闭
     */
    public boolean isShutdown() {
        return shutdown.get();
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("supportedProtocols", getSupportedProtocols().size());
        stats.put("activePoolCount", getPoolCount());
        stats.put("isRunning", started.get() && !shutdown.get());
        
        Map<String, Object> poolStats = new ConcurrentHashMap<>();
        for (Map.Entry<Protocol, ConnectionPool> entry : connectionPools.entrySet()) {
            try {
                poolStats.put(entry.getKey().type(), entry.getValue().getStatistics());
            } catch (Exception e) {
                log.debug("获取连接池统计异常", e);
            }
        }
        stats.put("pools", poolStats);
        
        return stats;
    }

    /**
     * 获取或创建连接池
     */
    private ConnectionPool getOrCreateConnectionPool(Protocol protocol) {
        return connectionPools.computeIfAbsent(protocol, p -> {
            ConnectionFactory factory = connectionFactories.get(p);
            if (factory == null) {
                throw new IllegalArgumentException("未找到协议的连接工厂: " + p.type());
            }
            
            ConnectionPool pool = new DefaultConnectionPool(factory, config);
            pool.init();
            
            if (started.get()) {
                pool.start();
            }
            
            log.debug("创建连接池: {}", p.type());
            return pool;
        });
    }
} 