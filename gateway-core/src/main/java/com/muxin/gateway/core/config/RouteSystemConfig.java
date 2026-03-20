package com.muxin.gateway.core.config;

import lombok.Builder;
import lombok.Data;

/**
 * 路由系统配置
 * 管理路由系统的全局配置参数，与 route 包下的 RouteConfig（路由实例配置）区分
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class RouteSystemConfig {
    
    @Builder.Default
    private Long defaultConnectionTimeout = 5000L;
    
    @Builder.Default
    private Long defaultRequestTimeout = 30000L;
    
    @Builder.Default
    private Long defaultTotalTimeout = 120000L;
    
    @Builder.Default
    private Long defaultReadTimeout = 30000L;
    
    @Builder.Default
    private Long defaultWriteTimeout = 30000L;
    
    @Builder.Default
    private Long defaultCircuitBreakerTimeout = 10000L;
    
    @Builder.Default
    private boolean enableDefaultTimeouts = true;
    
    @Builder.Default
    private int maxRoutes = 1000;
    
    @Builder.Default
    private boolean enableRouteCache = true;
    
    @Builder.Default
    private Long routeCacheExpiration = 600000L;
    
    @Builder.Default
    private boolean enableDynamicRouting = true;
    
    @Builder.Default
    private Long routeRefreshInterval = 30000L;
    
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
        if (defaultConnectionTimeout == null || defaultConnectionTimeout < 0) {
            throw new IllegalArgumentException("defaultConnectionTimeout 必须大于 0");
        }
        if (defaultRequestTimeout == null || defaultRequestTimeout < 0) {
            throw new IllegalArgumentException("defaultRequestTimeout 必须大于 0");
        }
        if (maxRoutes <= 0) {
            throw new IllegalArgumentException("maxRoutes 必须大于 0");
        }
        if (routeCacheExpiration == null || routeCacheExpiration < 0) {
            throw new IllegalArgumentException("routeCacheExpiration 必须大于 0");
        }
    }
}
