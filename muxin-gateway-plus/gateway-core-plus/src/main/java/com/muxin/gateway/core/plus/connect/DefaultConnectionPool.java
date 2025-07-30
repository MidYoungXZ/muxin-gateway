package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认连接池实现
 * 负责管理和复用网络连接，提高网关性能
 *
 * @author muxin
 */
@Slf4j
public class DefaultConnectionPool implements ConnectionPool {

    private final ConnectionFactory connectionFactory;
    private final ConnectionPoolConfig config;
    private final Map<String, BlockingQueue<Connection>> pools;
    private final ScheduledExecutorService cleanupExecutor;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // 统计信息
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public DefaultConnectionPool(ConnectionFactory connectionFactory) {
        this(connectionFactory, ConnectionPoolConfig.defaultConfig());
    }

    public DefaultConnectionPool(ConnectionFactory connectionFactory, ConnectionPoolConfig config) {
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.pools = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ConnectionPool-Cleanup");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPool] 连接池初始化");

            // 启动清理任务
            cleanupExecutor.scheduleWithFixedDelay(
                    this::cleanupIdleConnections,
                    30000, // 30秒后开始
                    30000, // 每30秒执行一次
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPool] 连接池启动");
        }
    }

    @Override
    public void shutdown() {
        if (closed.compareAndSet(false, true)) {
            log.info("[DefaultConnectionPool] 关闭连接池");
            cleanupExecutor.shutdown();

            // 关闭所有连接
            for (BlockingQueue<Connection> pool : pools.values()) {
                Connection conn;
                while ((conn = pool.poll()) != null) {
                    try {
                        conn.close();
                    } catch (Exception e) {
                        log.debug("关闭连接异常", e);
                    }
                }
            }
            pools.clear();
        }
    }

    @Override
    public Connection getConnection(EndpointAddress target, Protocol protocol) {
        return getConnection(target, protocol, 5000L);
    }

    @Override
    public Connection getConnection(EndpointAddress target, Protocol protocol, long timeoutMs) {
        if (isClosed()) {
            throw new IllegalStateException("连接池已关闭");
        }

        totalRequests.incrementAndGet();
        String poolKey = generatePoolKey(target, protocol);

        try {
            BlockingQueue<Connection> pool = getOrCreatePool(poolKey);

            // 尝试从池中获取连接
            Connection connection = pool.poll();
            if (connection != null && connection.isActive()) {
                if (connection instanceof ClientConnection && ((ClientConnection) connection).isHealthy()) {
                    ((ClientConnection) connection).markInUse();
                    return connection;
                } else {
                    // 连接不健康，销毁它
                    connection.close();
                }
            }

            // 创建新连接
            CompletableFuture<ClientConnection> future = connectionFactory.createClientConnection(target);
            ClientConnection newConnection = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            newConnection.markInUse();
            totalConnections.incrementAndGet();

            return newConnection;

        } catch (Exception e) {
            totalFailures.incrementAndGet();
            throw new RuntimeException("获取连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void returnConnection(Connection connection) {
        if (connection == null || isClosed()) {
            return;
        }

        try {
            // 无法归还，直接关闭
            connection.close();
        } catch (Exception e) {
            log.error("归还连接异常", e);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.debug("释放连接异常", e);
            }
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalConnections", totalConnections.get());
        stats.put("totalRequests", totalRequests.get());
        stats.put("totalFailures", totalFailures.get());
        stats.put("activePools", pools.size());

        long requests = totalRequests.get();
        if (requests > 0) {
            stats.put("successRate", 1.0 - (double) totalFailures.get() / requests);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getPoolStatus(EndpointAddress target) {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("target", target.toUri());

        String poolKey = generatePoolKey(target, null);
        BlockingQueue<Connection> pool = pools.get(poolKey);
        if (pool != null) {
            status.put("idleConnections", pool.size());
            status.put("exists", true);
        } else {
            status.put("idleConnections", 0);
            status.put("exists", false);
        }

        return status;
    }

    @Override
    public void warmup(EndpointAddress target, Protocol protocol, int minConnections) {
        String poolKey = generatePoolKey(target, protocol);
        BlockingQueue<Connection> pool = getOrCreatePool(poolKey);

        for (int i = 0; i < minConnections; i++) {
            try {
                CompletableFuture<ClientConnection> future = connectionFactory.createClientConnection(target);
                ClientConnection connection = future.get(5, TimeUnit.SECONDS);
                connection.markIdle();
                pool.offer(connection);
                totalConnections.incrementAndGet();
            } catch (Exception e) {
                log.warn("预热连接创建失败: {}", target.toUri(), e);
                break;
            }
        }

        log.info("连接池预热完成: {} - {}", target.toUri(), minConnections);
    }

    @Override
    public void cleanupIdleConnections() {
        if (isClosed()) {
            return;
        }

        for (Map.Entry<String, BlockingQueue<Connection>> entry : pools.entrySet()) {
            BlockingQueue<Connection> pool = entry.getValue();
            Connection connection;

            while ((connection = pool.peek()) != null) {
                long idleTime = System.currentTimeMillis() - connection.getLastActiveTime();
                if (idleTime > 60000) { // 60秒空闲超时
                    pool.poll();
                    try {
                        connection.close();
                    } catch (Exception e) {
                        log.debug("清理连接异常", e);
                    }
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void configure(ConnectionPoolConfig config) {
        log.info("连接池配置更新（暂不支持动态更新）");
    }

    @Override
    public ConnectionPoolConfig getConfig() {
        return config;
    }

    private String generatePoolKey(EndpointAddress target, Protocol protocol) {
        if (protocol != null) {
            return target.toUri() + "#" + protocol.type();
        }
        return target.toUri();
    }

    private BlockingQueue<Connection> getOrCreatePool(String poolKey) {
        return pools.computeIfAbsent(poolKey, k -> new LinkedBlockingQueue<>());
    }
}