package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 核心网关配置
 * 
 * @author muxin
 */
@Data
@Builder
public class GatewayCoreConfig {
    
    @Builder.Default
    private String gatewayName = "muxin-gateway";
    
    @Builder.Default
    private String version = "2.0";
    
    @Builder.Default
    private int workerThreads = Runtime.getRuntime().availableProcessors();
    
    @Builder.Default
    private Duration defaultTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private int maxRetries = 3;
    
    @Builder.Default
    private boolean enableTracing = true;
    
    @Builder.Default
    private boolean enableMetrics = true;
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Duration healthCheckInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private String traceIdHeader = "X-Trace-Id";
    
    @Builder.Default
    private boolean enableGlobalErrorHandler = true;
    
    public static GatewayCoreConfig defaultConfig() {
        return GatewayCoreConfig.builder().build();
    }
    
    public void validate() {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("gatewayName不能为空");
        }
        if (workerThreads <= 0) {
            throw new IllegalArgumentException("workerThreads必须大于0");
        }
        if (defaultTimeout == null || defaultTimeout.isNegative()) {
            throw new IllegalArgumentException("defaultTimeout必须大于0");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries不能小于0");
        }
    }
} 