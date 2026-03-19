package com.muxin.gateway.core.plus.connect.netty;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty 连接池管理器
 * 基于 FixedChannelPool 实现真正的连接复用
 * 简化版本：只支持 HTTP 协议
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class NettyConnectionPoolManager implements ConnectionPoolManager {

    private final NettyPoolConfig config;
    private final EventLoopGroup sharedEventLoopGroup;
    private final Map<String, NettyConnectionPool> pools;
    private final HttpChannelFactory channelFactory;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public NettyConnectionPoolManager() {
        this(NettyPoolConfig.defaultConfig());
    }

    public NettyConnectionPoolManager(NettyPoolConfig config) {
        this.config = config;
        this.sharedEventLoopGroup = new NioEventLoopGroup(config.getEventLoopThreads());
        this.pools = new ConcurrentHashMap<>();
        this.channelFactory = new HttpChannelFactory(config);
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            log.info("[NettyConnectionPoolManager] 初始化连接池管理器");
        }
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            log.info("[NettyConnectionPoolManager] 启动连接池管理器");

            for (NettyConnectionPool pool : pools.values()) {
                pool.start();
            }
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            log.info("[NettyConnectionPoolManager] 关闭连接池管理器");

            for (NettyConnectionPool pool : pools.values()) {
                pool.shutdown();
            }
            pools.clear();

            sharedEventLoopGroup.shutdownGracefully();

            log.info("[NettyConnectionPoolManager] 连接池管理器已关闭");
        }
    }

    @Override
    public ClientConnection getClientConnection(EndpointAddress target) {
        return getClientConnection(target, config.getAcquireTimeoutMs());
    }

    @Override
    public ClientConnection getClientConnection(EndpointAddress target, long timeoutMs) {
        if (isShutdown()) {
            throw new IllegalStateException("连接池管理器已关闭");
        }

        try {
            NettyConnectionPool pool = getOrCreatePool(target);
            Connection connection = pool.getConnection(target, timeoutMs);

            if (!(connection instanceof ClientConnection)) {
                throw new RuntimeException("获取的不是客户端连接");
            }

            return (ClientConnection) connection;
        } catch (Exception e) {
            throw new RuntimeException("获取客户端连接失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void returnConnection(Connection connection) {
        if (connection == null || isShutdown()) {
            return;
        }

        if (connection instanceof PooledClientConnection) {
            PooledClientConnection pooledConn = (PooledClientConnection) connection;
            pooledConn.returnToPool();
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        if (connection instanceof PooledClientConnection) {
            PooledClientConnection pooledConn = (PooledClientConnection) connection;
            pooledConn.destroy();
        }
    }

    @Override
    public void warmupPool(EndpointAddress target, int minConnections) {
        if (isShutdown()) {
            return;
        }

        try {
            NettyConnectionPool pool = getOrCreatePool(target);
            pool.warmup(target, minConnections);
        } catch (Exception e) {
            log.error("连接池预热失败", e);
        }
    }

    @Override
    public void removePool(EndpointAddress target) {
        String poolKey = generatePoolKey(target);
        NettyConnectionPool pool = pools.remove(poolKey);

        if (pool != null) {
            pool.shutdown();
            log.info("移除连接池：{}", poolKey);
        }
    }

    @Override
    public void cleanupIdleConnections() {
        if (isShutdown()) {
            return;
        }

        for (NettyConnectionPool pool : pools.values()) {
            pool.cleanupIdleConnections();
        }
    }

    @Override
    public void cleanupUnhealthyPools() {
        cleanupIdleConnections();
    }

    @Override
    public int getPoolCount() {
        return pools.size();
    }

    private NettyConnectionPool getOrCreatePool(EndpointAddress target) {
        String poolKey = generatePoolKey(target);

        return pools.computeIfAbsent(poolKey, key -> {
            NettyConnectionPool pool = new NettyConnectionPool(
                    target, config, channelFactory, sharedEventLoopGroup
            );
            pool.init();

            if (started.get()) {
                pool.start();
            }

            log.debug("创建连接池：{}", key);
            return pool;
        });
    }

    private String generatePoolKey(EndpointAddress target) {
        return target.toUri() + "#http";
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("poolCount", pools.size());
        stats.put("isRunning", started.get() && !shutdown.get());

        Map<String, Object> poolStats = new ConcurrentHashMap<>();
        for (Map.Entry<String, NettyConnectionPool> entry : pools.entrySet()) {
            poolStats.put(entry.getKey(), entry.getValue().getStatistics());
        }
        stats.put("pools", poolStats);

        return stats;
    }

    public boolean isShutdown() {
        return shutdown.get();
    }
}