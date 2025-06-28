package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 监控指标配置
 * 
 * @author muxin
 */
@Data
@Builder
public class MetricsConfig {
    
    @Builder.Default
    private boolean enableMetrics = true;
    
    @Builder.Default
    private boolean enableJvmMetrics = true;
    
    @Builder.Default
    private boolean enableSystemMetrics = true;
    
    @Builder.Default
    private boolean enableConnectionPoolMetrics = true;
    
    @Builder.Default
    private boolean enableRouteMetrics = true;
    
    @Builder.Default
    private boolean enableFilterMetrics = true;
    
    @Builder.Default
    private boolean enableLoadBalanceMetrics = true;
    
    @Builder.Default
    private Duration metricsInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration metricsRetentionTime = Duration.ofHours(1);
    
    @Builder.Default
    private String metricsPrefix = "muxin.gateway";
    
    @Builder.Default
    private boolean enablePrometheusExport = false;
    
    @Builder.Default
    private int prometheusPort = 9090;
    
    @Builder.Default
    private String prometheusPath = "/metrics";
    
    @Builder.Default
    private boolean enableHistograms = true;
    
    @Builder.Default
    private boolean enableCounters = true;
    
    @Builder.Default
    private boolean enableGauges = true;
    
    @Builder.Default
    private boolean enableTimers = true;
    
    public static MetricsConfig defaultConfig() {
        return MetricsConfig.builder().build();
    }
    
    public void validate() {
        if (metricsInterval == null || metricsInterval.isNegative()) {
            throw new IllegalArgumentException("metricsInterval必须大于0");
        }
        if (metricsRetentionTime == null || metricsRetentionTime.isNegative()) {
            throw new IllegalArgumentException("metricsRetentionTime必须大于0");
        }
        if (metricsPrefix == null || metricsPrefix.trim().isEmpty()) {
            throw new IllegalArgumentException("metricsPrefix不能为空");
        }
        if (prometheusPort <= 0 || prometheusPort > 65535) {
            throw new IllegalArgumentException("prometheusPort必须在1-65535之间");
        }
    }
} 