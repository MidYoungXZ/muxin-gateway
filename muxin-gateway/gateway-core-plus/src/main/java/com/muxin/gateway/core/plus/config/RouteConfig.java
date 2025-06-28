package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 路由配置
 * 
 * @author muxin
 */
@Data
@Builder
public class RouteConfig {
    
    @Builder.Default
    private Duration defaultConnectionTimeout = Duration.ofSeconds(5);
    
    @Builder.Default
    private Duration defaultRequestTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration defaultTotalTimeout = Duration.ofMinutes(2);
    
    @Builder.Default
    private Duration defaultReadTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration defaultWriteTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration defaultCircuitBreakerTimeout = Duration.ofSeconds(10);
    
    @Builder.Default
    private boolean enableDefaultTimeouts = true;
    
    @Builder.Default
    private int maxRoutes = 1000;
    
    @Builder.Default
    private boolean enableRouteCache = true;
    
    @Builder.Default
    private Duration routeCacheExpiration = Duration.ofMinutes(10);
    
    @Builder.Default
    private boolean enableDynamicRouting = true;
    
    @Builder.Default
    private Duration routeRefreshInterval = Duration.ofSeconds(30);
    
    @Builder.Default
    private boolean enableRouteMetrics = true;
    
    @Builder.Default
    private boolean enableStrictPathMatching = false;
    
    @Builder.Default
    private boolean enableCaseSensitiveMatching = false;
    
    public static RouteConfig defaultConfig() {
        return RouteConfig.builder().build();
    }
    
    public void validate() {
        if (defaultConnectionTimeout == null || defaultConnectionTimeout.isNegative()) {
            throw new IllegalArgumentException("defaultConnectionTimeout必须大于0");
        }
        if (defaultRequestTimeout == null || defaultRequestTimeout.isNegative()) {
            throw new IllegalArgumentException("defaultRequestTimeout必须大于0");
        }
        if (maxRoutes <= 0) {
            throw new IllegalArgumentException("maxRoutes必须大于0");
        }
        if (routeCacheExpiration == null || routeCacheExpiration.isNegative()) {
            throw new IllegalArgumentException("routeCacheExpiration必须大于0");
        }
    }
} 