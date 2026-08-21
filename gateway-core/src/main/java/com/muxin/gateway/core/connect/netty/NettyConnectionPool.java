package com.muxin.gateway.core.connect.netty;

import com.muxin.gateway.core.connect.Connection;
import com.muxin.gateway.core.connect.ConnectionPool;
import com.muxin.gateway.core.service.EndpointAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.FixedChannelPool.AcquireTimeoutAction;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP Netty连接池实现
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class NettyConnectionPool implements ConnectionPool {

    private final String poolKey;
    private final EndpointAddress target;
    private final NettyPoolConfig config;
    private final HttpChannelFactory channelFactory;
    private final EventLoopGroup eventLoopGroup;
    private final boolean sharedEventLoopGroup;

    private FixedChannelPool channelPool;
    private Bootstrap bootstrap;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final AtomicLong totalAcquires = new AtomicLong(0);
    private final AtomicLong totalReleases = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public NettyConnectionPool(EndpointAddress target,
                               NettyPoolConfig config,
                               HttpChannelFactory channelFactory) {
        this(target, config, channelFactory, null);
    }

    public NettyConnectionPool(EndpointAddress target,
                               NettyPoolConfig config,
                               HttpChannelFactory channelFactory,
                               EventLoopGroup sharedEventLoopGroup) {
        this.poolKey = generatePoolKey(target);
        this.target = target;
        this.config = config;
        this.channelFactory = channelFactory;
        this.sharedEventLoopGroup = sharedEventLoopGroup != null;
        this.eventLoopGroup = sharedEventLoopGroup != null
                ? sharedEventLoopGroup
                : new NioEventLoopGroup(config.getEventLoopThreads());
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            log.info("[NettyConnectionPool] 初始化连接池: {}", poolKey);
            bootstrap = createBootstrap();
            channelPool = createChannelPool();
            log.info("[NettyConnectionPool] 连接池初始化完成: maxConnections={}, maxPendingAcquires={}",
                    config.getMaxConnections(), config.getMaxPendingAcquires());
        }
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            log.info("[NettyConnectionPool] 连接池启动: {}", poolKey);
        }
    }

    @Override
    public void shutdown() {
        if (closed.compareAndSet(false, true)) {
            log.info("[NettyConnectionPool] 关闭连接池: {}", poolKey);
            if (channelPool != null) {
                channelPool.close();
            }
            if (!sharedEventLoopGroup && eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            }
            log.info("[NettyConnectionPool] 连接池已关闭: acquires={}, releases={}, failures={}",
                    totalAcquires.get(), totalReleases.get(), totalFailures.get());
        }
    }

    @Override
    public Connection getConnection(EndpointAddress target) {
        return getConnection(target, config.getAcquireTimeoutMs());
    }

    @Override
    public Connection getConnection(EndpointAddress target, long timeoutMs) {
        if (isClosed()) {
            throw new IllegalStateException("连接池已关闭: " + poolKey);
        }
        totalAcquires.incrementAndGet();
        try {
            Future<Channel> future = channelPool.acquire();
            boolean completed = future.await(timeoutMs, TimeUnit.MILLISECONDS);
            if (!completed) {
                totalFailures.incrementAndGet();
                throw new RuntimeException("获取连接超时");
            }
            if (!future.isSuccess()) {
                totalFailures.incrementAndGet();
                throw new RuntimeException("获取连接失败: " + (future.cause() != null ? future.cause().getMessage() : "unknown"));
            }
            Channel channel = future.getNow();
            if (channel == null) {
                totalFailures.incrementAndGet();
                throw new RuntimeException("获取连接返回null");
            }
            return new PooledClientConnection(channel, this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            totalFailures.incrementAndGet();
            throw new RuntimeException("获取连接被中断", e);
        } catch (Exception e) {
            totalFailures.incrementAndGet();
            throw new RuntimeException("获取连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        if (connection instanceof PooledClientConnection) {
            PooledClientConnection pooledConn = (PooledClientConnection) connection;
            Channel channel = pooledConn.getChannel();
            if (channel != null) {
                returnChannel(channel);
                log.debug("释放连接: {}", channel);
            }
        }
    }

    @Override
    public void removeConnection(Connection connection) {
        releaseConnection(connection);
    }

    @Override
    public int getActiveCount() {
        if (channelPool != null) {
            return channelPool.acquiredChannelCount();
        }
        return 0;
    }

    @Override
    public int getIdleCount() {
        // FixedChannelPool不支持直接获取空闲连接数
        // 使用总连接数减去活跃连接数作为估算
        int maxConnections = config.getMaxConnections();
        int active = getActiveCount();
        return Math.max(0, maxConnections - active);
    }

    @Override
    public int getTotalCount() {
        return getActiveCount() + getIdleCount();
    }

    @Override
    public Map<String, Object> getPoolStatus(EndpointAddress target) {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("target", target.toUri());
        status.put("exists", true);
        status.put("closed", isClosed());
        status.put("started", started.get());
        status.put("active", getActiveCount());
        status.put("idle", getIdleCount());
        status.put("total", getTotalCount());
        return status;
    }

    @Override
    public void warmup(EndpointAddress target, int minConnections) {
        log.info("[NettyConnectionPool] 预热连接池: {} - {} connections", poolKey, minConnections);
        for (int i = 0; i < minConnections; i++) {
            try {
                Future<Channel> future = channelPool.acquire();
                boolean completed = future.await(5, TimeUnit.SECONDS);
                if (completed && future.isSuccess()) {
                    Channel channel = future.getNow();
                    channelPool.release(channel);
                    log.debug("预热连接创建成功: {}", channel);
                }
            } catch (Exception e) {
                log.warn("预热连接创建失败: {}", e.getMessage());
                break;
            }
        }
    }

    @Override
    public void cleanupIdleConnections() {
        log.debug("[NettyConnectionPool] 清理空闲连接: {}", poolKey);
    }

    void returnChannel(Channel channel) {
        if (channel != null && !isClosed()) {
            channelPool.release(channel);
            totalReleases.incrementAndGet();
        }
    }

    void destroyChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close().addListener(ignored -> returnChannel(channel));
            log.debug("[NettyConnectionPool] 销毁连接: {}", channel.id().asShortText());
        } catch (Exception e) {
            log.warn("[NettyConnectionPool] 销毁连接失败: {}", e.getMessage());
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("poolKey", poolKey);
        stats.put("target", target.toUri());
        stats.put("maxConnections", config.getMaxConnections());
        stats.put("acquires", totalAcquires.get());
        stats.put("releases", totalReleases.get());
        stats.put("failures", totalFailures.get());
        stats.put("active", getActiveCount());
        stats.put("idle", getIdleCount());
        long acquires = totalAcquires.get();
        if (acquires > 0) {
            stats.put("successRate", 1.0 - (double) totalFailures.get() / acquires);
        }
        return stats;
    }

    private Bootstrap createBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        Map<ChannelOption<?>, Object> options = config.getChannelOptions();
        for (Map.Entry<ChannelOption<?>, Object> entry : options.entrySet()) {
            @SuppressWarnings("unchecked")
            ChannelOption<Object> option = (ChannelOption<Object>) entry.getKey();
            bootstrap.option(option, entry.getValue());
        }
        bootstrap.remoteAddress(target.getHost(), target.getPort());
        channelFactory.configureBootstrap(bootstrap, config);
        return bootstrap;
    }

    private FixedChannelPool createChannelPool() {
        ChannelHealthChecker healthChecker = channelFactory.createHealthChecker();
        return new FixedChannelPool(
                bootstrap,
                channelFactory.createPoolHandler(),
                healthChecker,
                AcquireTimeoutAction.NEW,
                config.getAcquireTimeoutMs(),
                config.getMaxConnections(),
                config.getMaxPendingAcquires(),
                config.isReleaseHealthCheck(),
                config.isLastRecentUsed()
        );
    }

    private String generatePoolKey(EndpointAddress target) {
        return target.toUri() + "#http";
    }
}
