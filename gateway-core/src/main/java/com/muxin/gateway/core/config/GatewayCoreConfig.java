package com.muxin.gateway.core.config;

import lombok.Builder;
import lombok.Data;

/**
 * 核心网关配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
    private Long defaultTimeout = 30000L;
    
    @Builder.Default
    private int maxRetries = 3;
    
    @Builder.Default
    private boolean enableTracing = true;
    
    @Builder.Default
    private boolean enableMetrics = true;
    
    @Builder.Default
    private boolean enableHealthCheck = true;
    
    @Builder.Default
    private Long healthCheckInterval = 30000L;
    
    @Builder.Default
    private String traceIdHeader = "X-Trace-Id";
    
    @Builder.Default
    private boolean enableGlobalErrorHandler = true;
    
    public static GatewayCoreConfig defaultConfig() {
        return GatewayCoreConfig.builder().build();
    }
    
    public void validate() {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("gatewayName 不能为空");
        }
        if (workerThreads <= 0) {
            throw new IllegalArgumentException("workerThreads 必须大于 0");
        }
        if (defaultTimeout == null || defaultTimeout < 0) {
            throw new IllegalArgumentException("defaultTimeout 必须大于 0");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries 不能小于 0");
        }
    }
}
