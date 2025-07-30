package com.muxin.gateway.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.muxin.gateway.core.monitor.PerformanceMetrics;
import com.muxin.gateway.core.route.RouteRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 路由缓存
 * 使用Caffeine高性能缓存库缓存路由匹配结果
 */
@Slf4j
@Component
public class RouteCache {
    
    /**
     * 路由缓存
     * key: 请求路径
     * value: 匹配的路由规则
     */
    private final Cache<String, RouteRule> routeCache;
    
    /**
     * 缓存统计信息
     */
    private final Cache<String, Long> hitCountCache;
    
    /**
     * 性能监控（可选）
     */
    @Autowired(required = false)
    private PerformanceMetrics performanceMetrics;
    
    public RouteCache() {
        // 初始化路由缓存
        this.routeCache = Caffeine.newBuilder()
                .maximumSize(10000)  // 最大缓存10000个路由
                .expireAfterWrite(5, TimeUnit.MINUTES)  // 5分钟后过期
                .recordStats()  // 记录统计信息
                .build();
        
        // 初始化命中统计缓存
        this.hitCountCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }
    
    /**
     * 获取缓存的路由规则
     * @param path 请求路径
     * @return 路由规则，如果不存在返回null
     */
    public RouteRule get(String path) {
        RouteRule rule = routeCache.getIfPresent(path);
        if (rule != null) {
            // 增加命中计数
            hitCountCache.put(path, hitCountCache.get(path, k -> 0L) + 1);
            // 记录缓存命中
            if (performanceMetrics != null) {
                performanceMetrics.recordCacheHit();
            }
        } else {
            // 记录缓存未命中
            if (performanceMetrics != null) {
                performanceMetrics.recordCacheMiss();
            }
        }
        return rule;
    }
    
    /**
     * 缓存路由规则
     * @param path 请求路径
     * @param rule 路由规则
     */
    public void put(String path, RouteRule rule) {
        if (path != null && rule != null) {
            routeCache.put(path, rule);
        }
    }
    
    /**
     * 清除指定路径的缓存
     * @param path 请求路径
     */
    public void invalidate(String path) {
        routeCache.invalidate(path);
        hitCountCache.invalidate(path);
    }
    
    /**
     * 清除所有缓存
     */
    public void invalidateAll() {
        routeCache.invalidateAll();
        hitCountCache.invalidateAll();
        log.info("Route cache cleared");
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    public CacheStats getStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = routeCache.stats();
        return new CacheStats(
                stats.hitCount(),
                stats.missCount(),
                stats.loadSuccessCount(),
                stats.loadFailureCount(),
                stats.totalLoadTime(),
                stats.evictionCount(),
                routeCache.estimatedSize()
        );
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final long hitCount;
        private final long missCount;
        private final long loadSuccessCount;
        private final long loadFailureCount;
        private final long totalLoadTime;
        private final long evictionCount;
        private final long size;
        
        public CacheStats(long hitCount, long missCount, long loadSuccessCount,
                         long loadFailureCount, long totalLoadTime, long evictionCount, long size) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.loadSuccessCount = loadSuccessCount;
            this.loadFailureCount = loadFailureCount;
            this.totalLoadTime = totalLoadTime;
            this.evictionCount = evictionCount;
            this.size = size;
        }
        
        public double getHitRate() {
            long requestCount = hitCount + missCount;
            return requestCount == 0 ? 0.0 : (double) hitCount / requestCount;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hitCount=%d, missCount=%d, hitRate=%.2f%%, size=%d}",
                    hitCount, missCount, getHitRate() * 100, size);
        }
    }
} 