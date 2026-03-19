package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

/**
 * 节点管理器配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class NodeManagerConfig {
    
    @Builder.Default
    private boolean enableServiceDiscovery = true;
    
    @Builder.Default
    private Long serviceDiscoveryInterval = 30000L;
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Long healthCheckInterval = 30000L;
    
    @Builder.Default
    private Long healthCheckTimeout = 5000L;
    
    @Builder.Default
    private int maxFailureCount = 3;
    
    @Builder.Default
    private Long failureRecoveryTime = 60000L;
    
    @Builder.Default
    private boolean enableNodeCache = true;
    
    @Builder.Default
    private Long nodeCacheExpiration = 600000L;
    
    @Builder.Default
    private int maxNodesPerService = 100;
    
    @Builder.Default
    private boolean enableNodeMetrics = true;
    
    @Builder.Default
    private Long nodeMetricsInterval = 60000L;
    
    @Builder.Default
    private boolean enableAutoDeregistration = true;
    
    @Builder.Default
    private Long autoDeregistrationDelay = 300000L;
    
    public static NodeManagerConfig defaultConfig() {
        return NodeManagerConfig.builder().build();
    }
    
    public void validate() {
        if (serviceDiscoveryInterval == null || serviceDiscoveryInterval < 0) {
            throw new IllegalArgumentException("serviceDiscoveryInterval 必须大于 0");
        }
        if (healthCheckInterval == null || healthCheckInterval < 0) {
            throw new IllegalArgumentException("healthCheckInterval 必须大于 0");
        }
        if (healthCheckTimeout == null || healthCheckTimeout < 0) {
            throw new IllegalArgumentException("healthCheckTimeout 必须大于 0");
        }
        if (maxFailureCount < 0) {
            throw new IllegalArgumentException("maxFailureCount 不能小于 0");
        }
        if (maxNodesPerService <= 0) {
            throw new IllegalArgumentException("maxNodesPerService 必须大于 0");
        }
    }
}
