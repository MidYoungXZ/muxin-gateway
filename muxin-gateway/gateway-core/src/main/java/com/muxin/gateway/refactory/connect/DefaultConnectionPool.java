package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.ProtocolAdapter;
import com.muxin.gateway.refactory.node.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认连接池实现
 * 提供高性能的连接管理和复用功能
 *
 * @author muxin
 */
@Slf4j
public class DefaultConnectionPool implements ConnectionPool {
    
    private final Map<String, TargetConnectionPool> targetPools;
    private final Map<Protocol, ProtocolAdapter> protocolAdapters;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong totalConnectionsCreated;
    private final AtomicLong totalConnectionsAcquired;
    private final AtomicLong totalConnectionsReturned;
    private final AtomicLong totalConnectionsReleased;
    
    private volatile ConnectionPoolConfig config;
    private volatile boolean closed = false;
    
    public DefaultConnectionPool() {
        this(ConnectionPoolConfig.defaultConfig());
    }
    
    public DefaultConnectionPool(ConnectionPoolConfig config) {
        this.config = config;
        this.targetPools = new ConcurrentHashMap<>();
        this.protocolAdapters = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ConnectionPool-Scheduler");
            t.setDaemon(true);
            return t;
        });
        this.totalConnectionsCreated = new AtomicLong(0);
        this.totalConnectionsAcquired = new AtomicLong(0);
        this.totalConnectionsReturned = new AtomicLong(0);
        this.totalConnectionsReleased = new AtomicLong(0);
        
        // 启动定期清理任务
        startCleanupTask();
        
        log.info("连接池初始化完成，配置: {}", config);
    }
    
    @Override
    public CompletableFuture<Connection> getConnection(EndpointAddress target, Protocol protocol) {
        return getConnection(target, protocol, config.getAcquireTimeout().toMillis());
    }
    
    @Override
    public CompletableFuture<Connection> getConnection(EndpointAddress target, Protocol protocol, long timeoutMs) {
        if (closed) {
            return CompletableFuture.failedFuture(new IllegalStateException("连接池已关闭"));
        }
        
        String poolKey = getPoolKey(target, protocol);
        TargetConnectionPool targetPool = targetPools.computeIfAbsent(poolKey, 
            k -> new TargetConnectionPool(target, protocol));
        
        CompletableFuture<Connection> future = targetPool.acquireConnection(timeoutMs);
        future.whenComplete((conn, ex) -> {
            if (ex == null) {
                totalConnectionsAcquired.incrementAndGet();
                log.debug("获取连接成功: {} -> {}", poolKey, conn.getConnectionId());
            } else {
                log.error("获取连接失败: {} - {}", poolKey, ex.getMessage());
            }
        });
        
        return future;
    }
    
    @Override
    public void returnConnection(Connection connection) {
        if (connection == null || closed) {
            return;
        }
        
        String poolKey = getPoolKey(connection.getRemoteAddress(), connection.getProtocol());
        TargetConnectionPool targetPool = targetPools.get(poolKey);
        
        if (targetPool != null) {
            targetPool.returnConnection(connection);
            totalConnectionsReturned.incrementAndGet();
            log.debug("归还连接: {} -> {}", poolKey, connection.getConnectionId());
        } else {
            // 池不存在，直接关闭连接
            connection.close();
            log.warn("归还连接到不存在的池: {}", poolKey);
        }
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        
        String poolKey = getPoolKey(connection.getRemoteAddress(), connection.getProtocol());
        TargetConnectionPool targetPool = targetPools.get(poolKey);
        
        if (targetPool != null) {
            targetPool.releaseConnection(connection);
        } else {
            connection.close();
        }
        
        totalConnectionsReleased.incrementAndGet();
        log.debug("释放连接: {} -> {}", poolKey, connection.getConnectionId());
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTargets", targetPools.size());
        stats.put("totalConnectionsCreated", totalConnectionsCreated.get());
        stats.put("totalConnectionsAcquired", totalConnectionsAcquired.get());
        stats.put("totalConnectionsReturned", totalConnectionsReturned.get());
        stats.put("totalConnectionsReleased", totalConnectionsReleased.get());
        stats.put("isClosed", closed);
        stats.put("config", config);
        
        // 计算总的活跃和空闲连接数
        int totalActiveConnections = 0;
        int totalIdleConnections = 0;
        for (TargetConnectionPool pool : targetPools.values()) {
            totalActiveConnections += pool.getActiveConnectionCount();
            totalIdleConnections += pool.getIdleConnectionCount();
        }
        stats.put("totalActiveConnections", totalActiveConnections);
        stats.put("totalIdleConnections", totalIdleConnections);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getPoolStatus(EndpointAddress target) {
        String poolKey = getPoolKey(target, null);
        return targetPools.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(poolKey))
                .findFirst()
                .map(entry -> entry.getValue().getStatistics())
                .orElse(new HashMap<>());
    }
    
    @Override
    public CompletableFuture<Void> warmup(EndpointAddress target, Protocol protocol, int minConnections) {
        if (closed || !config.isEnableWarmup()) {
            return CompletableFuture.completedFuture(null);
        }
        
        String poolKey = getPoolKey(target, protocol);
        TargetConnectionPool targetPool = targetPools.computeIfAbsent(poolKey, 
            k -> new TargetConnectionPool(target, protocol));
        
        return targetPool.warmup(minConnections);
    }
    
    @Override
    public void cleanupIdleConnections() {
        if (closed) {
            return;
        }
        
        log.debug("开始清理空闲连接");
        int cleanedCount = 0;
        
        for (TargetConnectionPool pool : targetPools.values()) {
            cleanedCount += pool.cleanupIdleConnections();
        }
        
        log.debug("清理空闲连接完成，清理数量: {}", cleanedCount);
    }
    
    @Override
    public CompletableFuture<Void> close() {
        if (closed) {
            return CompletableFuture.completedFuture(null);
        }
        
        closed = true;
        log.info("开始关闭连接池");
        
        // 关闭调度器
        scheduler.shutdown();
        
        // 关闭所有目标连接池
        List<CompletableFuture<Void>> closeFutures = new ArrayList<>();
        for (TargetConnectionPool pool : targetPools.values()) {
            closeFutures.add(pool.close());
        }
        
        return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    targetPools.clear();
                    log.info("连接池关闭完成");
                });
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void configure(ConnectionPoolConfig config) {
        this.config = config;
        log.info("连接池配置已更新: {}", config);
    }
    
    @Override
    public ConnectionPoolConfig getConfig() {
        return config;
    }
    
    /**
     * 注册协议适配器
     */
    public void registerProtocolAdapter(ProtocolAdapter adapter) {
        protocolAdapters.put(adapter.getSupportedProtocol(), adapter);
        log.info("注册协议适配器: {}", adapter.getSupportedProtocol().getName());
    }
    
    /**
     * 获取池键
     */
    private String getPoolKey(EndpointAddress target, Protocol protocol) {
        if (protocol != null) {
            return target.toUri() + ":" + protocol.getName();
        }
        return target.toUri();
    }
    
    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        scheduler.scheduleWithFixedDelay(
            this::cleanupIdleConnections,
            config.getCleanupInterval().toMillis(),
            config.getCleanupInterval().toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        if (config.isEnableHealthCheck()) {
            scheduler.scheduleWithFixedDelay(
                this::performHealthCheck,
                config.getHealthCheckInterval().toMillis(),
                config.getHealthCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS
            );
        }
    }
    
    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        if (closed) {
            return;
        }
        
        log.debug("开始连接健康检查");
        for (TargetConnectionPool pool : targetPools.values()) {
            pool.performHealthCheck();
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    /**
     * 目标连接池实现
     */
    private class TargetConnectionPool {
        private final EndpointAddress target;
        private final Protocol protocol;
        private final Queue<PooledConnection> idleConnections;
        private final Set<PooledConnection> activeConnections;
        private final AtomicInteger connectionCount;
        private final CompletableFuture<Void> closeFuture;
        
        public TargetConnectionPool(EndpointAddress target, Protocol protocol) {
            this.target = target;
            this.protocol = protocol;
            this.idleConnections = new ConcurrentLinkedQueue<>();
            this.activeConnections = ConcurrentHashMap.newKeySet();
            this.connectionCount = new AtomicInteger(0);
            this.closeFuture = new CompletableFuture<>();
        }
        
        public CompletableFuture<Connection> acquireConnection(long timeoutMs) {
            // 尝试从空闲连接中获取
            PooledConnection pooledConn = idleConnections.poll();
            if (pooledConn != null && pooledConn.isValid()) {
                activeConnections.add(pooledConn);
                return CompletableFuture.completedFuture(pooledConn.getConnection());
            }
            
            // 如果没有空闲连接且未达到最大连接数，创建新连接
            if (connectionCount.get() < config.getMaxConnectionsPerTarget()) {
                return createNewConnection();
            }
            
            // 等待连接可用
            return waitForConnection(timeoutMs);
        }
        
        public void returnConnection(Connection connection) {
            PooledConnection pooledConn = findPooledConnection(connection);
            if (pooledConn != null) {
                activeConnections.remove(pooledConn);
                if (pooledConn.isValid() && config.isEnableConnectionReuse()) {
                    pooledConn.updateLastUsedTime();
                    idleConnections.offer(pooledConn);
                } else {
                    closeConnection(pooledConn);
                }
            }
        }
        
        public void releaseConnection(Connection connection) {
            PooledConnection pooledConn = findPooledConnection(connection);
            if (pooledConn != null) {
                activeConnections.remove(pooledConn);
                closeConnection(pooledConn);
            }
        }
        
        public CompletableFuture<Void> warmup(int minConnections) {
            List<CompletableFuture<Connection>> futures = new ArrayList<>();
            
            for (int i = 0; i < minConnections && connectionCount.get() < config.getMaxConnectionsPerTarget(); i++) {
                futures.add(createNewConnection());
            }
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> log.debug("连接池预热完成: {} -> {} 个连接", target.toUri(), futures.size()));
        }
        
        public int cleanupIdleConnections() {
            int cleanedCount = 0;
            long currentTime = System.currentTimeMillis();
            long idleTimeoutMs = config.getIdleTimeout().toMillis();
            long maxLifetimeMs = config.getMaxLifetime().toMillis();
            
            Iterator<PooledConnection> iterator = idleConnections.iterator();
            while (iterator.hasNext()) {
                PooledConnection pooledConn = iterator.next();
                
                if (!pooledConn.isValid() ||
                    (currentTime - pooledConn.getLastUsedTime()) > idleTimeoutMs ||
                    (currentTime - pooledConn.getCreatedTime()) > maxLifetimeMs) {
                    
                    iterator.remove();
                    closeConnection(pooledConn);
                    cleanedCount++;
                }
            }
            
            return cleanedCount;
        }
        
        public void performHealthCheck() {
            // 检查空闲连接的健康状态
            idleConnections.removeIf(pooledConn -> {
                if (!pooledConn.isValid() || !pooledConn.getConnection().isActive()) {
                    closeConnection(pooledConn);
                    return true;
                }
                return false;
            });
        }
        
        public CompletableFuture<Void> close() {
            // 关闭所有连接
            List<CompletableFuture<Void>> closeFutures = new ArrayList<>();
            
            // 关闭空闲连接
            PooledConnection pooledConn;
            while ((pooledConn = idleConnections.poll()) != null) {
                closeFutures.add(pooledConn.getConnection().close());
            }
            
            // 关闭活跃连接
            for (PooledConnection activeConn : activeConnections) {
                closeFutures.add(activeConn.getConnection().close());
            }
            activeConnections.clear();
            
            return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]))
                    .whenComplete((v, ex) -> closeFuture.complete(null));
        }
        
        public int getActiveConnectionCount() {
            return activeConnections.size();
        }
        
        public int getIdleConnectionCount() {
            return idleConnections.size();
        }
        
        public Map<String, Object> getStatistics() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("target", target.toUri());
            stats.put("protocol", protocol.getName());
            stats.put("totalConnections", connectionCount.get());
            stats.put("activeConnections", getActiveConnectionCount());
            stats.put("idleConnections", getIdleConnectionCount());
            stats.put("maxConnections", config.getMaxConnectionsPerTarget());
            return stats;
        }
        
        private CompletableFuture<Connection> createNewConnection() {
            ProtocolAdapter adapter = protocolAdapters.get(protocol);
            if (adapter == null) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("未找到协议适配器: " + protocol.getName()));
            }
            
            try {
                Connection connection = adapter.createConnection(target, new HashMap<>());
                if (connection != null) {
                    PooledConnection pooledConn = new PooledConnection(connection);
                    connectionCount.incrementAndGet();
                    totalConnectionsCreated.incrementAndGet();
                    activeConnections.add(pooledConn);
                    return CompletableFuture.completedFuture(connection);
                }
            } catch (Exception e) {
                log.error("创建连接失败: {} - {}", target.toUri(), e.getMessage());
            }
            
            return CompletableFuture.failedFuture(
                new RuntimeException("无法创建连接到: " + target.toUri()));
        }
        
        private CompletableFuture<Connection> waitForConnection(long timeoutMs) {
            // 简化实现：直接返回失败
            return CompletableFuture.failedFuture(
                new TimeoutException("获取连接超时: " + target.toUri()));
        }
        
        private PooledConnection findPooledConnection(Connection connection) {
            return activeConnections.stream()
                    .filter(pc -> pc.getConnection().equals(connection))
                    .findFirst()
                    .orElse(null);
        }
        
        private void closeConnection(PooledConnection pooledConn) {
            connectionCount.decrementAndGet();
            pooledConn.getConnection().close();
        }
    }
    
    /**
     * 池化连接包装器
     */
    private static class PooledConnection {
        private final Connection connection;
        private final long createdTime;
        private volatile long lastUsedTime;
        
        public PooledConnection(Connection connection) {
            this.connection = connection;
            this.createdTime = System.currentTimeMillis();
            this.lastUsedTime = createdTime;
        }
        
        public Connection getConnection() {
            return connection;
        }
        
        public long getCreatedTime() {
            return createdTime;
        }
        
        public long getLastUsedTime() {
            return lastUsedTime;
        }
        
        public void updateLastUsedTime() {
            this.lastUsedTime = System.currentTimeMillis();
        }
        
        public boolean isValid() {
            return connection != null && connection.isActive();
        }
    }
} 