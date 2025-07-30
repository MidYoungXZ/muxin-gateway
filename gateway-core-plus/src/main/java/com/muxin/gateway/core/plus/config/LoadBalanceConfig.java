package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 负载均衡配置
 * 
 * @author muxin
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
    private Duration stickySessionExpiration = Duration.ofMinutes(30);
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Duration healthCheckInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private int maxFailureCount = 3;
    
    @Builder.Default
    private Duration failureRecoveryTime = Duration.ofMinutes(1);
    
    @Builder.Default
    private boolean enableNodeWeighting = false;
    
    @Builder.Default
    private int defaultNodeWeight = 100;
    
    @Builder.Default
    private boolean enableDynamicWeighting = false;
    
    @Builder.Default
    private Duration weightingUpdateInterval = Duration.ofMinutes(5);
    
    public static LoadBalanceConfig defaultConfig() {
        return LoadBalanceConfig.builder().build();
    }
    
    public void validate() {
        if (defaultStrategy == null || defaultStrategy.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultStrategy不能为空");
        }
        if (maxFailureCount < 0) {
            throw new IllegalArgumentException("maxFailureCount不能小于0");
        }
        if (defaultNodeWeight <= 0) {
            throw new IllegalArgumentException("defaultNodeWeight必须大于0");
        }
        if (healthCheckInterval == null || healthCheckInterval.isNegative()) {
            throw new IllegalArgumentException("healthCheckInterval必须大于0");
        }
    }
}