package com.muxin.gateway.core.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能指标收集器
 */
@Slf4j
@Component
public class PerformanceMetrics implements InitializingBean {
    
    private final MeterRegistry registry;
    
    // 请求计数器
    private final Counter totalRequestCounter;
    private final Counter successRequestCounter;
    private final Counter failedRequestCounter;
    
    // 响应时间计时器
    private final Timer responseTimer;
    
    // 路由缓存命中率
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    
    // 活跃连接数
    private final AtomicLong activeConnections = new AtomicLong(0);
    
    // 按路径统计的计数器
    private final ConcurrentHashMap<String, Counter> pathCounters = new ConcurrentHashMap<>();
    
    // 按状态码统计的计数器
    private final ConcurrentHashMap<Integer, Counter> statusCounters = new ConcurrentHashMap<>();
    
    public PerformanceMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        // 初始化计数器
        this.totalRequestCounter = Counter.builder("gateway.requests.total")
                .description("Total number of requests")
                .register(registry);
                
        this.successRequestCounter = Counter.builder("gateway.requests.success")
                .description("Number of successful requests")
                .register(registry);
                
        this.failedRequestCounter = Counter.builder("gateway.requests.failed")
                .description("Number of failed requests")
                .register(registry);
                
        // 初始化响应时间计时器
        this.responseTimer = Timer.builder("gateway.response.time")
                .description("Response time")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
                
        // 初始化缓存计数器
        this.cacheHitCounter = Counter.builder("gateway.cache.hits")
                .description("Cache hit count")
                .register(registry);
                
        this.cacheMissCounter = Counter.builder("gateway.cache.misses")
                .description("Cache miss count")
                .register(registry);
                
        // 注册活跃连接数指标
        registry.gauge("gateway.connections.active", activeConnections);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册JVM指标
        new JvmGcMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        
        log.info("Performance metrics initialized");
    }
    
    /**
     * 记录请求
     */
    public void recordRequest(String path) {
        totalRequestCounter.increment();
        
        // 按路径统计
        pathCounters.computeIfAbsent(path, p -> 
            Counter.builder("gateway.requests.path")
                    .tag("path", p)
                    .description("Requests per path")
                    .register(registry)
        ).increment();
    }
    
    /**
     * 记录成功请求
     */
    public void recordSuccess(int statusCode) {
        successRequestCounter.increment();
        recordStatusCode(statusCode);
    }
    
    /**
     * 记录失败请求
     */
    public void recordFailure(int statusCode) {
        failedRequestCounter.increment();
        recordStatusCode(statusCode);
    }
    
    /**
     * 记录响应时间
     */
    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }
    
    /**
     * 停止计时并记录
     */
    public void recordResponseTime(Timer.Sample sample) {
        sample.stop(responseTimer);
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMissCounter.increment();
    }
    
    /**
     * 增加活跃连接数
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    /**
     * 减少活跃连接数
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * 记录状态码
     */
    private void recordStatusCode(int statusCode) {
        statusCounters.computeIfAbsent(statusCode, code -> 
            Counter.builder("gateway.responses.status")
                    .tag("status", String.valueOf(code))
                    .description("Responses per status code")
                    .register(registry)
        ).increment();
    }
    
    /**
     * 获取当前QPS
     */
    public double getCurrentQps() {
        return totalRequestCounter.count() / 
               (System.currentTimeMillis() / 1000.0);
    }
    
    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        double total = totalRequestCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return successRequestCounter.count() / total;
    }
    
    /**
     * 获取缓存命中率
     */
    public double getCacheHitRate() {
        double total = cacheHitCounter.count() + cacheMissCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return cacheHitCounter.count() / total;
    }
} 