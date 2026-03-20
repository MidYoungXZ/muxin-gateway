package com.muxin.gateway.core.config;

import lombok.Builder;
import lombok.Data;

/**
 * 过滤器配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
    private Long defaultFilterTimeout = 5000L;
    
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
            throw new IllegalArgumentException("maxFiltersPerRoute 必须大于 0");
        }
        if (defaultFilterTimeout == null || defaultFilterTimeout < 0) {
            throw new IllegalArgumentException("defaultFilterTimeout 必须大于 0");
        }
        if (asyncFilterThreads <= 0) {
            throw new IllegalArgumentException("asyncFilterThreads 必须大于 0");
        }
    }
}