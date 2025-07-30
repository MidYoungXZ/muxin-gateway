package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 路由系统配置
 * 管理路由系统的全局配置参数，与route包下的RouteConfig（路由实例配置）区分
 * 
 * @author muxin
 */
@Data
@Builder
public class RouteSystemConfig {
    
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
    
    public static RouteSystemConfig defaultConfig() {
        return RouteSystemConfig.builder().build();
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