package com.muxin.gateway.core.connect.netty;

import io.netty.channel.ChannelOption;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty 连接池配置
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class NettyPoolConfig {

    @Builder.Default
    private int maxConnections = Math.max(100, Runtime.getRuntime().availableProcessors() * 10);

    @Builder.Default
    private int maxPendingAcquires = 100;

    @Builder.Default
    private long acquireTimeoutMs = 30000;

    @Builder.Default
    private int connectTimeoutMs = 10000;

    @Builder.Default
    private boolean keepAlive = true;

    @Builder.Default
    private boolean tcpNoDelay = true;

    @Builder.Default
    private boolean soReuseAddr = true;

    @Builder.Default
    private int sendBufferSize = 64 * 1024;

    @Builder.Default
    private int receiveBufferSize = 64 * 1024;

    @Builder.Default
    private int eventLoopThreads = Runtime.getRuntime().availableProcessors();

    @Builder.Default
    private boolean releaseHealthCheck = true;

    @Builder.Default
    private boolean lastRecentUsed = true;

    @Builder.Default
    private boolean enableWarmup = false;

    @Builder.Default
    private boolean enableHealthCheck = true;

    @Builder.Default
    private int minConnections = Math.max(10, Runtime.getRuntime().availableProcessors() * 2);

    @Builder.Default
    private int maxContentLength = 10 * 1024 * 1024;

    @Builder.Default
    private int maxHeaderSize = 8192;

    @Builder.Default
    private int maxChunkSize = 8192;

    @Builder.Default
    private int maxInitialLineLength = 4096;

    @Builder.Default
    private Long idleTimeout = 300000L;

    @Builder.Default
    private Long maxLifetime = 1800000L;

    public static NettyPoolConfig defaultConfig() {
        return NettyPoolConfig.builder().build();
    }

    public static NettyPoolConfig highPerformanceConfig() {
        int processors = Runtime.getRuntime().availableProcessors();
        return NettyPoolConfig.builder()
                .maxConnections(processors * 20)
                .minConnections(processors * 5)
                .maxPendingAcquires(200)
                .acquireTimeoutMs(3000)
                .connectTimeoutMs(3000)
                .eventLoopThreads(processors * 2)
                .build();
    }

    public static NettyPoolConfig lowLatencyConfig() {
        int processors = Runtime.getRuntime().availableProcessors();
        return NettyPoolConfig.builder()
                .maxConnections(processors * 50)
                .minConnections(processors * 10)
                .maxPendingAcquires(500)
                .acquireTimeoutMs(1000)
                .connectTimeoutMs(1000)
                .build();
    }

    public Map<ChannelOption<?>, Object> getChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs);
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.TCP_NODELAY, tcpNoDelay);
        options.put(ChannelOption.SO_REUSEADDR, soReuseAddr);
        options.put(ChannelOption.SO_SNDBUF, sendBufferSize);
        options.put(ChannelOption.SO_RCVBUF, receiveBufferSize);
        return Collections.unmodifiableMap(options);
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public long getAcquireTimeoutMs() {
        return acquireTimeoutMs;
    }

    public int getMaxPendingAcquires() {
        return maxPendingAcquires;
    }

    public boolean isReleaseHealthCheck() {
        return releaseHealthCheck;
    }

    public boolean isLastRecentUsed() {
        return lastRecentUsed;
    }

    public int getEventLoopThreads() {
        return eventLoopThreads;
    }

    public int getMinConnections() {
        return minConnections;
    }
}
