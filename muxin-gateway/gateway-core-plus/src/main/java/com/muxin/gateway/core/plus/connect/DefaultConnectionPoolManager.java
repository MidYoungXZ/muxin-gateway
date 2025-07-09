package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.connect.http.HttpConnectionFactory;
import com.muxin.gateway.core.plus.connect.http.HttpConnectionPool;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认连接池管理器实现
 * 
 * @author muxin
 */
@Slf4j
public class DefaultConnectionPoolManager implements ConnectionPoolManager {
    
    private final Map<String, HttpConnectionPool> poolMap;
    private final ConnectionPoolConfig config;
    private final HttpConnectionFactory connectionFactory;
    private final ScheduledExecutorService scheduler;
    
    private volatile boolean running = false;
    
    public DefaultConnectionPoolManager(ConnectionPoolConfig config) {
        this.config = config;
        this.poolMap = new ConcurrentHashMap<>();
        this.connectionFactory = new HttpConnectionFactory();
        this.scheduler = Executors.newScheduledThreadPool(2,
            r -> new Thread(r, "DefaultConnectionPoolManager-scheduler"));
        
        log.info("[DefaultConnectionPoolManager] 连接池管理器创建完成");
    }
    
    @Override
    public CompletableFuture<ClientConnection> getClientConnection(EndpointAddress target, Protocol protocol) {
        return getClientConnection(target, protocol, config.getAcquireTimeout());
    }
    
    @Override
    public CompletableFuture<ClientConnection> getClientConnection(EndpointAddress target, 
                                                                   Protocol protocol, 
                                                                   Duration timeout) {
        HttpConnectionPool pool = getOrCreatePool(target, protocol);
        // 将HttpClientConnection转换为ClientConnection
        return pool.getConnection(target, protocol, timeout.toMillis())
            .thenApply(connection -> (ClientConnection) connection);
    }
    
    @Override
    public void returnConnection(Connection connection) {
        if (connection instanceof ClientConnection) {
            ClientConnection clientConn = (ClientConnection) connection;
            clientConn.returnToPool();
        }
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        if (connection instanceof ClientConnection) {
            ClientConnection clientConn = (ClientConnection) connection;
            clientConn.destroy();
        }
    }
    
    @Override
    public CompletableFuture<Void> warmupPool(EndpointAddress target, Protocol protocol, int minConnections) {
        HttpConnectionPool pool = getOrCreatePool(target, protocol);
        return pool.warmup(target, protocol, minConnections);
    }
    
    @Override
    public void removePool(EndpointAddress target, Protocol protocol) {
        String poolKey = buildPoolKey(target, protocol);
        HttpConnectionPool pool = poolMap.remove(poolKey);
        
        if (pool != null) {
            pool.close();
            log.info("[DefaultConnectionPoolManager] 移除连接池: {}", target.toUri());
        }
    }
    
    @Override
    public void cleanupIdleConnections() {
        for (HttpConnectionPool pool : poolMap.values()) {
            pool.cleanupIdleConnections();
        }
        log.debug("[DefaultConnectionPoolManager] 清理空闲连接完成 - 连接池数量: {}", poolMap.size());
    }
    
    @Override
    public void cleanupUnhealthyPools() {
        poolMap.entrySet().removeIf(entry -> {
            HttpConnectionPool pool = entry.getValue();
            if (!pool.isHealthy()) {
                pool.close();
                log.info("[DefaultConnectionPoolManager] 清理不健康连接池: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("running", running);
        stats.put("poolCount", poolMap.size());
        stats.put("config", config);
        
        // 聚合统计
        int totalConnections = 0;
        int activeConnections = 0;
        int idleConnections = 0;
        long totalRequests = 0;
        
        for (HttpConnectionPool pool : poolMap.values()) {
            Map<String, Object> poolStats = pool.getStatistics();
            totalConnections += (Integer) poolStats.getOrDefault("totalConnections", 0);
            activeConnections += (Integer) poolStats.getOrDefault("activeConnections", 0);
            idleConnections += (Integer) poolStats.getOrDefault("idleConnections", 0);
            totalRequests += (Long) poolStats.getOrDefault("requestCount", 0L);
        }
        
        stats.put("totalConnections", totalConnections);
        stats.put("activeConnections", activeConnections);
        stats.put("idleConnections", idleConnections);
        stats.put("totalRequests", totalRequests);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getPoolStatistics(EndpointAddress target, Protocol protocol) {
        String poolKey = buildPoolKey(target, protocol);
        HttpConnectionPool pool = poolMap.get(poolKey);
        return pool != null ? pool.getStatistics() : Map.of();
    }
    
    @Override
    public Map<String, Boolean> getPoolHealthStatus() {
        Map<String, Boolean> healthMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, HttpConnectionPool> entry : poolMap.entrySet()) {
            healthMap.put(entry.getKey(), entry.getValue().isHealthy());
        }
        return healthMap;
    }
    
    @Override
    public int getPoolCount() {
        return poolMap.size();
    }
    
    @Override
    public boolean supportsProtocol(Protocol protocol) {
        // 目前只支持HTTP协议
        return "HTTP".equalsIgnoreCase(protocol.getName()) || 
               "HTTPS".equalsIgnoreCase(protocol.getName());
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        return Set.of(
            new Protocol.HttpProtocol(),
            new Protocol.HttpProtocol() // HTTPS也使用HTTP协议，只是传输层加密
        );
    }
    
    // ========== LifeCycle接口实现 ==========
    
    @Override
    public void init() {
        if (running) {
            return;
        }
        
        // 启动连接工厂
        connectionFactory.start();
        
        // 启动定时清理任务
        scheduler.scheduleWithFixedDelay(
            this::cleanupIdleConnections,
            config.getCleanupInterval().getSeconds(),
            config.getCleanupInterval().getSeconds(),
            TimeUnit.SECONDS
        );
        
        // 启动连接池健康检查
        scheduler.scheduleWithFixedDelay(
            this::cleanupUnhealthyPools,
            60, // 1分钟间隔
            60,
            TimeUnit.SECONDS
        );
        
        log.info("[DefaultConnectionPoolManager] 连接池管理器初始化完成");
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        init();
        running = true;
        
        // 启动所有已存在的连接池
        for (HttpConnectionPool pool : poolMap.values()) {
            pool.start();
        }
        
        log.info("[DefaultConnectionPoolManager] 连接池管理器启动完成");
    }
    
    @Override
    public void shutdown() {
        if (!running) {
            return;
        }
        
        running = false;
        
        log.info("[DefaultConnectionPoolManager] 开始关闭连接池管理器 - 连接池数量: {}", poolMap.size());
        
        // 关闭所有连接池
        CompletableFuture<?>[] closeFutures = poolMap.values().stream()
            .map(HttpConnectionPool::close)
            .toArray(CompletableFuture[]::new);
        
        try {
            CompletableFuture.allOf(closeFutures).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[DefaultConnectionPoolManager] 关闭连接池超时", e);
        }
        
        poolMap.clear();
        
        // 关闭连接工厂
        connectionFactory.stop();
        
        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("[DefaultConnectionPoolManager] 连接池管理器关闭完成");
    }
    
    // ========== 私有方法 ==========
    
    private HttpConnectionPool getOrCreatePool(EndpointAddress target, Protocol protocol) {
        String poolKey = buildPoolKey(target, protocol);
        
        return poolMap.computeIfAbsent(poolKey, key -> {
            HttpConnectionPool pool = new HttpConnectionPool(
                target, protocol, config, connectionFactory);
            
            if (running) {
                pool.init();
                pool.start();
            }
            
            log.info("[DefaultConnectionPoolManager] 创建新连接池: {}", target.toUri());
            return pool;
        });
    }
    
    private String buildPoolKey(EndpointAddress target, Protocol protocol) {
        return target.getHost() + ":" + target.getPort() + ":" + protocol.getName();
    }
}