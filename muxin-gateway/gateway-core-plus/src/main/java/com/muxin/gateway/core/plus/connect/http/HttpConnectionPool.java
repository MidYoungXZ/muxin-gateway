package com.muxin.gateway.core.plus.connect.http;

import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPool;
import com.muxin.gateway.core.plus.connect.ConnectionPoolConfig;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.node.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP连接池实现
 * 管理单个目标地址的HTTP连接
 * 
 * @author muxin
 */
@Slf4j
public class HttpConnectionPool implements ConnectionPool {
    
    private final EndpointAddress target;
    private final Protocol protocol;
    private final ConnectionPoolConfig config;
    private final HttpConnectionFactory connectionFactory;
    
    // 连接管理
    private final Queue<HttpClientConnection> availableConnections;
    private final Set<HttpClientConnection> allConnections;
    private final AtomicInteger activeConnectionCount;
    private final AtomicInteger totalConnectionCount;
    
    // 等待队列
    private final BlockingQueue<CompletableFuture<Connection>> waitingQueue;
    
    // 统计信息
    private final AtomicLong requestCount;
    private final AtomicLong connectionCreatedCount;
    private final AtomicLong connectionFailedCount;
    private final AtomicLong totalWaitTime;
    private final AtomicLong totalConnectionTime;
    
    // 线程池和调度器
    private final ScheduledExecutorService scheduler;
    private final ExecutorService connectionExecutor;
    
    // 状态管理
    private volatile boolean closed = false;
    private volatile boolean initialized = false;
    private final Object lock = new Object();
    
    public HttpConnectionPool(EndpointAddress target, 
                             Protocol protocol,
                             ConnectionPoolConfig config,
                             HttpConnectionFactory connectionFactory) {
        this.target = target;
        this.protocol = protocol;
        this.config = config != null ? config : ConnectionPoolConfig.defaultConfig();
        this.connectionFactory = connectionFactory;
        
        this.availableConnections = new ConcurrentLinkedQueue<>();
        this.allConnections = ConcurrentHashMap.newKeySet();
        this.activeConnectionCount = new AtomicInteger(0);
        this.totalConnectionCount = new AtomicInteger(0);
        
        this.waitingQueue = new LinkedBlockingQueue<>();
        
        this.requestCount = new AtomicLong(0);
        this.connectionCreatedCount = new AtomicLong(0);
        this.connectionFailedCount = new AtomicLong(0);
        this.totalWaitTime = new AtomicLong(0);
        this.totalConnectionTime = new AtomicLong(0);
        
        this.scheduler = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "HttpConnectionPool-" + target.toUri() + "-scheduler"));
        this.connectionExecutor = Executors.newCachedThreadPool(
            r -> new Thread(r, "HttpConnectionPool-" + target.toUri() + "-connection"));
        
        log.info("[HttpConnectionPool] 创建连接池: {} - 配置: {}", target.toUri(), config);
    }
    
    @Override
    public CompletableFuture<Connection> getConnection(EndpointAddress target, Protocol protocol) {
        return getConnection(target, protocol, config.getAcquireTimeout().toMillis());
    }
    
    @Override
    public CompletableFuture<Connection> getConnection(EndpointAddress target, 
                                                      Protocol protocol, 
                                                      long timeoutMs) {
        if (closed) {
            return CompletableFuture.failedFuture(new IllegalStateException("连接池已关闭"));
        }
        
        long startTime = System.currentTimeMillis();
        requestCount.incrementAndGet();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = doGetConnection(timeoutMs);
                long waitTime = System.currentTimeMillis() - startTime;
                totalWaitTime.addAndGet(waitTime);
                
                log.debug("[HttpConnectionPool] 获取连接成功: {} - 等待时间: {}ms", 
                    target.toUri(), waitTime);
                return connection;
                
            } catch (Exception e) {
                long waitTime = System.currentTimeMillis() - startTime;
                totalWaitTime.addAndGet(waitTime);
                
                log.error("[HttpConnectionPool] 获取连接失败: {} - 等待时间: {}ms", 
                    target.toUri(), waitTime, e);
                throw new RuntimeException("获取连接失败", e);
            }
        }, connectionExecutor);
    }
    
    private Connection doGetConnection(long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        
        while (System.currentTimeMillis() < deadline) {
            // 1. 尝试从空闲连接池获取
            HttpClientConnection connection = getAvailableConnection();
            if (connection != null) {
                connection.markInUse();
                activeConnectionCount.incrementAndGet();
                return connection;
            }
            
            // 2. 尝试创建新连接
            if (canCreateNewConnection()) {
                connection = createNewConnection();
                if (connection != null) {
                    connection.markInUse();
                    activeConnectionCount.incrementAndGet();
                    totalConnectionCount.incrementAndGet();
                    allConnections.add(connection);
                    connectionCreatedCount.incrementAndGet();
                    return connection;
                }
            }
            
            // 3. 等待连接可用
            synchronized (lock) {
                try {
                    lock.wait(Math.min(100, deadline - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待连接被中断", e);
                }
            }
        }
        
        throw new RuntimeException("获取连接超时: " + timeoutMs + "ms");
    }
    
    private HttpClientConnection getAvailableConnection() {
        HttpClientConnection connection;
        while ((connection = availableConnections.poll()) != null) {
            if (connection.isHealthy() && connection.isActive()) {
                return connection;
            } else {
                // 移除不健康的连接
                removeConnection(connection);
            }
        }
        return null;
    }
    
    private boolean canCreateNewConnection() {
        return totalConnectionCount.get() < config.getMaxConnectionsPerTarget();
    }
    
    private HttpClientConnection createNewConnection() {
        try {
            long startTime = System.currentTimeMillis();
            
            HttpClientConnection connection = connectionFactory.createConnection(
                target, protocol, this);
            
            long connectionTime = System.currentTimeMillis() - startTime;
            totalConnectionTime.addAndGet(connectionTime);
            
            log.debug("[HttpConnectionPool] 创建新连接: {} - 耗时: {}ms", 
                target.toUri(), connectionTime);
            
            return connection;
            
        } catch (Exception e) {
            connectionFailedCount.incrementAndGet();
            log.error("[HttpConnectionPool] 创建连接失败: {}", target.toUri(), e);
            return null;
        }
    }
    
    @Override
    public void returnConnection(Connection connection) {
        if (connection instanceof HttpClientConnection) {
            returnConnection((HttpClientConnection) connection);
        }
    }
    
    public void returnConnection(HttpClientConnection connection) {
        if (connection == null || closed) {
            return;
        }
        
        if (!allConnections.contains(connection)) {
            log.warn("[HttpConnectionPool] 归还的连接不属于此池: {}", connection.getConnectionId());
            return;
        }
        
        connection.markIdle();
        activeConnectionCount.decrementAndGet();
        
        if (connection.isHealthy() && connection.isActive()) {
            availableConnections.offer(connection);
            
            // 通知等待的请求
            synchronized (lock) {
                lock.notifyAll();
            }
            
            log.debug("[HttpConnectionPool] 连接归还成功: {}", connection.getConnectionId());
        } else {
            // 移除不健康的连接
            removeConnection(connection);
        }
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        if (connection instanceof HttpClientConnection) {
            releaseConnection((HttpClientConnection) connection);
        }
    }
    
    public void releaseConnection(HttpClientConnection connection) {
        if (connection == null) {
            return;
        }
        
        if (allConnections.contains(connection)) {
            if (connection.isInUse()) {
                activeConnectionCount.decrementAndGet();
            }
            removeConnection(connection);
        }
        
        log.debug("[HttpConnectionPool] 连接释放: {}", connection.getConnectionId());
    }
    
    private void removeConnection(HttpClientConnection connection) {
        if (connection == null) {
            return;
        }
        
        allConnections.remove(connection);
        availableConnections.remove(connection);
        totalConnectionCount.decrementAndGet();
        
        // 异步关闭连接
        connection.destroy();
        
        log.debug("[HttpConnectionPool] 移除连接: {}", connection.getConnectionId());
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("target", target.toUri());
        stats.put("totalConnections", totalConnectionCount.get());
        stats.put("activeConnections", activeConnectionCount.get());
        stats.put("idleConnections", availableConnections.size());
        stats.put("requestCount", requestCount.get());
        stats.put("connectionCreatedCount", connectionCreatedCount.get());
        stats.put("connectionFailedCount", connectionFailedCount.get());
        stats.put("averageWaitTime", calculateAverageWaitTime());
        stats.put("averageConnectionTime", calculateAverageConnectionTime());
        stats.put("closed", closed);
        return stats;
    }
    
    @Override
    public Map<String, Object> getPoolStatus(EndpointAddress target) {
        if (!this.target.equals(target)) {
            return null;
        }
        return getStatistics();
    }
    
    @Override
    public CompletableFuture<Void> warmup(EndpointAddress target, Protocol protocol, int minConnections) {
        if (!this.target.equals(target) || closed) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            int currentSize = totalConnectionCount.get();
            int connectionsToCreate = Math.max(0, minConnections - currentSize);
            
            log.info("[HttpConnectionPool] 开始预热连接池: {} - 当前: {}, 目标: {}", 
                target.toUri(), currentSize, minConnections);
            
            for (int i = 0; i < connectionsToCreate; i++) {
                if (closed) break;
                
                try {
                    HttpClientConnection connection = createNewConnection();
                    if (connection != null) {
                        allConnections.add(connection);
                        availableConnections.offer(connection);
                        totalConnectionCount.incrementAndGet();
                        connectionCreatedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("[HttpConnectionPool] 预热连接失败: {}", target.toUri(), e);
                }
            }
            
            log.info("[HttpConnectionPool] 连接池预热完成: {} - 最终连接数: {}", 
                target.toUri(), totalConnectionCount.get());
        }, connectionExecutor);
    }
    
    @Override
    public void cleanupIdleConnections() {
        if (closed) {
            return;
        }
        
        long now = System.currentTimeMillis();
        long idleTimeout = config.getIdleTimeout().toMillis();
        int minConnections = config.getMinConnectionsPerTarget();
        
        // 清理空闲超时的连接
        availableConnections.removeIf(connection -> {
            long idleTime = now - connection.getLastActiveTime();
            boolean shouldRemove = idleTime > idleTimeout && 
                                 totalConnectionCount.get() > minConnections;
            
            if (shouldRemove) {
                removeConnection(connection);
                log.debug("[HttpConnectionPool] 清理空闲连接: {} - 空闲时间: {}ms", 
                    connection.getConnectionId(), idleTime);
                return true;
            }
            return false;
        });
        
        // 清理不健康的连接
        allConnections.removeIf(connection -> {
            if (!connection.isHealthy()) {
                if (connection.isInUse()) {
                    activeConnectionCount.decrementAndGet();
                }
                availableConnections.remove(connection);
                totalConnectionCount.decrementAndGet();
                connection.destroy();
                
                log.debug("[HttpConnectionPool] 清理不健康连接: {}", connection.getConnectionId());
                return true;
            }
            return false;
        });
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            closed = true;
            
            log.info("[HttpConnectionPool] 开始关闭连接池: {}", target.toUri());
            
            // 关闭所有连接
            for (HttpClientConnection connection : allConnections) {
                connection.destroy();
            }
            
            availableConnections.clear();
            allConnections.clear();
            
            // 关闭线程池
            scheduler.shutdown();
            connectionExecutor.shutdown();
            
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                if (!connectionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    connectionExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                connectionExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            log.info("[HttpConnectionPool] 连接池关闭完成: {}", target.toUri());
        });
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void configure(ConnectionPoolConfig config) {
        // 动态配置更新（简化实现）
        log.info("[HttpConnectionPool] 更新配置: {} - 新配置: {}", target.toUri(), config);
    }
    
    @Override
    public ConnectionPoolConfig getConfig() {
        return config;
    }
    
    @Override
    public void init() {
        if (initialized) {
            return;
        }
        
        synchronized (this) {
            if (initialized) {
                return;
            }
            
            // 启动定时清理任务
            scheduler.scheduleWithFixedDelay(
                this::cleanupIdleConnections,
                config.getCleanupInterval().getSeconds(),
                config.getCleanupInterval().getSeconds(),
                TimeUnit.SECONDS
            );
            
            // 预热连接池
            if (config.isEnableWarmup()) {
                warmup(target, protocol, config.getMinConnectionsPerTarget());
            }
            
            initialized = true;
            log.info("[HttpConnectionPool] 连接池初始化完成: {}", target.toUri());
        }
    }
    
    @Override
    public void start() {
        init();
        log.info("[HttpConnectionPool] 连接池启动: {}", target.toUri());
    }
    
    @Override
    public void shutdown() {
        close();
    }
    
    // 辅助方法
    
    private double calculateAverageWaitTime() {
        long requests = requestCount.get();
        return requests > 0 ? (double) totalWaitTime.get() / requests : 0.0;
    }
    
    private double calculateAverageConnectionTime() {
        long created = connectionCreatedCount.get();
        return created > 0 ? (double) totalConnectionTime.get() / created : 0.0;
    }
    
    /**
     * 获取连接池健康状态
     */
    public boolean isHealthy() {
        if (closed) return false;
        
        // 检查是否有可用连接或能创建新连接
        return !availableConnections.isEmpty() || canCreateNewConnection();
    }
    
    /**
     * 获取目标地址
     */
    public EndpointAddress getTarget() {
        return target;
    }
    
    /**
     * 获取协议
     */
    public Protocol getProtocol() {
        return protocol;
    }
} 