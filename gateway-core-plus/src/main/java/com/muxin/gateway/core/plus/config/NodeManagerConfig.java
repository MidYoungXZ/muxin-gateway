package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 节点管理器配置
 * 
 * @author muxin
 */
@Data
@Builder
public class NodeManagerConfig {
    
    @Builder.Default
    private boolean enableServiceDiscovery = true;
    
    @Builder.Default
    private Duration serviceDiscoveryInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Duration healthCheckInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration healthCheckTimeout = Duration.ofSeconds(5);
    
    @Builder.Default
    private int maxFailureCount = 3;
    
    @Builder.Default
    private Duration failureRecoveryTime = Duration.ofMinutes(1);
    
    @Builder.Default
    private boolean enableNodeCache = true;
    
    @Builder.Default
    private Duration nodeCacheExpiration = Duration.ofMinutes(10);
    
    @Builder.Default
    private int maxNodesPerService = 100;
    
    @Builder.Default
    private boolean enableNodeMetrics = true;
    
    @Builder.Default
    private Duration nodeMetricsInterval = Duration.ofMinutes(1);
    
    @Builder.Default
    private boolean enableAutoDeregistration = true;
    
    @Builder.Default
    private Duration autoDeregistrationDelay = Duration.ofMinutes(5);
    
    public static NodeManagerConfig defaultConfig() {
        return NodeManagerConfig.builder().build();
    }
    
    public void validate() {
        if (serviceDiscoveryInterval == null || serviceDiscoveryInterval.isNegative()) {
            throw new IllegalArgumentException("serviceDiscoveryInterval必须大于0");
        }
        if (healthCheckInterval == null || healthCheckInterval.isNegative()) {
            throw new IllegalArgumentException("healthCheckInterval必须大于0");
        }
        if (healthCheckTimeout == null || healthCheckTimeout.isNegative()) {
            throw new IllegalArgumentException("healthCheckTimeout必须大于0");
        }
        if (maxFailureCount < 0) {
            throw new IllegalArgumentException("maxFailureCount不能小于0");
        }
        if (maxNodesPerService <= 0) {
            throw new IllegalArgumentException("maxNodesPerService必须大于0");
        }
    }
}