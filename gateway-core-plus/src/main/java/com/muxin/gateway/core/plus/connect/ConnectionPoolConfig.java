package com.muxin.gateway.core.plus.connect;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 连接池配置
 *
 * @author muxin
 */
@Data
@Builder
public class ConnectionPoolConfig {
    
    /**
     * 每个目标地址的最大连接数
     */
    @Builder.Default
    private int maxConnectionsPerTarget = 10;
    
    /**
     * 每个目标地址的最小连接数
     */
    @Builder.Default
    private int minConnectionsPerTarget = 2;
    
    /**
     * 连接超时时间
     */
    @Builder.Default
    private Duration connectionTimeout = Duration.ofSeconds(5);
    
    /**
     * 连接空闲超时时间
     */
    @Builder.Default
    private Duration idleTimeout = Duration.ofMinutes(5);
    
    /**
     * 获取连接的等待超时时间
     */
    @Builder.Default
    private Duration acquireTimeout = Duration.ofSeconds(10);
    
    /**
     * 连接存活时间
     */
    @Builder.Default
    private Duration maxLifetime = Duration.ofMinutes(30);
    
    /**
     * 健康检查间隔
     */
    @Builder.Default
    private Duration healthCheckInterval = Duration.ofSeconds(30);
    
    /**
     * 是否启用连接预热
     */
    @Builder.Default
    private boolean enableWarmup = true;
    
    /**
     * 是否启用连接健康检查
     */
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    /**
     * 是否启用连接重用
     */
    @Builder.Default
    private boolean enableConnectionReuse = true;
    
    /**
     * 清理空闲连接的频率
     */
    @Builder.Default
    private Duration cleanupInterval = Duration.ofMinutes(1);
    
    /**
     * 连接池关闭时的等待时间
     */
    @Builder.Default
    private Duration shutdownTimeout = Duration.ofSeconds(30);
    
    /**
     * 创建默认配置
     */
    public static ConnectionPoolConfig defaultConfig() {
        return ConnectionPoolConfig.builder().build();
    }
    
    /**
     * 创建高性能配置
     */
    public static ConnectionPoolConfig highPerformanceConfig() {
        return ConnectionPoolConfig.builder()
                .maxConnectionsPerTarget(20)
                .minConnectionsPerTarget(5)
                .connectionTimeout(Duration.ofSeconds(3))
                .idleTimeout(Duration.ofMinutes(10))
                .acquireTimeout(Duration.ofSeconds(5))
                .maxLifetime(Duration.ofHours(1))
                .healthCheckInterval(Duration.ofSeconds(15))
                .cleanupInterval(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * 创建低延迟配置
     */
    public static ConnectionPoolConfig lowLatencyConfig() {
        return ConnectionPoolConfig.builder()
                .maxConnectionsPerTarget(50)
                .minConnectionsPerTarget(10)
                .connectionTimeout(Duration.ofSeconds(1))
                .idleTimeout(Duration.ofMinutes(2))
                .acquireTimeout(Duration.ofSeconds(2))
                .enableWarmup(true)
                .healthCheckInterval(Duration.ofSeconds(10))
                .cleanupInterval(Duration.ofSeconds(15))
                .build();
    }
} 