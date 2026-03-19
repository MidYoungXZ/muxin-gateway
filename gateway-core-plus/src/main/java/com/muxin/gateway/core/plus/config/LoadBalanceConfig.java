package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

/**
 * 负载均衡配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class LoadBalanceConfig {
    
    @Builder.Default
    private String defaultStrategy = "ROUND_ROBIN";
    
    @Builder.Default
    private boolean enableStickySession = false;
    
    @Builder.Default
    private String stickySessionKey = "sessionId";
    
    @Builder.Default
    private Long stickySessionExpiration = 1800000L;
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Long healthCheckInterval = 30000L;
    
    @Builder.Default
    private int maxFailureCount = 3;
    
    @Builder.Default
    private Long failureRecoveryTime = 60000L;
    
    @Builder.Default
    private boolean enableNodeWeighting = false;
    
    @Builder.Default
    private int defaultNodeWeight = 100;
    
    @Builder.Default
    private boolean enableDynamicWeighting = false;
    
    @Builder.Default
    private Long weightingUpdateInterval = 300000L;
    
    public static LoadBalanceConfig defaultConfig() {
        return LoadBalanceConfig.builder().build();
    }
    
    public void validate() {
        if (defaultStrategy == null || defaultStrategy.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultStrategy 不能为空");
        }
        if (maxFailureCount < 0) {
            throw new IllegalArgumentException("maxFailureCount 不能小于 0");
        }
        if (defaultNodeWeight <= 0) {
            throw new IllegalArgumentException("defaultNodeWeight 必须大于 0");
        }
        if (healthCheckInterval == null || healthCheckInterval < 0) {
            throw new IllegalArgumentException("healthCheckInterval 必须大于 0");
        }
    }
}
