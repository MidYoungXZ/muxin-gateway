package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 过滤器配置
 * 
 * @author muxin
 */
@Data
@Builder
public class FilterConfig {
    
    @Builder.Default
    private boolean enableGlobalFilters = true;
    
    @Builder.Default
    private boolean enableRouteFilters = true;
    
    @Builder.Default
    private int maxFiltersPerRoute = 50;
    
    @Builder.Default
    private Duration defaultFilterTimeout = Duration.ofSeconds(5);
    
    @Builder.Default
    private boolean enableFilterMetrics = true;
    
    @Builder.Default
    private boolean enableFilterTracing = true;
    
    @Builder.Default
    private boolean enableFilterErrorHandling = true;
    
    @Builder.Default
    private boolean skipFiltersOnError = false;
    
    @Builder.Default
    private boolean enableAsyncFilters = true;
    
    @Builder.Default
    private int asyncFilterThreads = Runtime.getRuntime().availableProcessors();
    
    public static FilterConfig defaultConfig() {
        return FilterConfig.builder().build();
    }
    
    public void validate() {
        if (maxFiltersPerRoute <= 0) {
            throw new IllegalArgumentException("maxFiltersPerRoute必须大于0");
        }
        if (defaultFilterTimeout == null || defaultFilterTimeout.isNegative()) {
            throw new IllegalArgumentException("defaultFilterTimeout必须大于0");
        }
        if (asyncFilterThreads <= 0) {
            throw new IllegalArgumentException("asyncFilterThreads必须大于0");
        }
    }
}