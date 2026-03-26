package com.muxin.gateway.core.route.filter;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FilterRegistry {

    private static final FilterRegistry INSTANCE = new FilterRegistry();
    
    private final Map<String, FilterFactory> factories = new ConcurrentHashMap<>();

    private FilterRegistry() {
        registerBuiltInFactories();
    }

    public static FilterRegistry getInstance() {
        return INSTANCE;
    }

    private void registerBuiltInFactories() {
        register(new AddRequestHeaderFilter.Factory());
        register(new AddResponseHeaderFilter.Factory());
        register(new RemoveRequestHeaderFilter.Factory());
        register(new RemoveResponseHeaderFilter.Factory());
        register(new RewritePathFilter.Factory());
        register(new RequestRateLimiterFilter.Factory());
        register(new CircuitBreakerFilter.Factory());
        register(new RetryFilter.Factory());
        register(new PathRewriteFilter.Factory());
        register(new RequestIdFilter.Factory());
        register(new RequestLogFilter.Factory());
        register(new MetricsFilter.Factory());
        
        log.info("[FilterRegistry] 已注册 {} 个内置过滤器工厂", factories.size());
    }

    public void register(FilterFactory factory) {
        String name = factory.getSupportedFilterName();
        factories.put(name, factory);
        log.debug("[FilterRegistry] 注册过滤器工厂: {}", name);
    }

    public FilterFactory getFactory(String filterName) {
        return factories.get(filterName);
    }

    public Filter createFilter(FilterDefinition definition) {
        String filterName = definition.getName();
        FilterFactory factory = factories.get(filterName);
        
        if (factory == null) {
            log.warn("[FilterRegistry] 未找到过滤器工厂: {}, 使用默认实现", filterName);
            return null;
        }
        
        try {
            factory.validateConfig(definition);
            return factory.createFilter(definition);
        } catch (Exception e) {
            log.error("[FilterRegistry] 创建过滤器失败: {}", filterName, e);
            throw new RuntimeException("创建过滤器失败: " + filterName, e);
        }
    }

    public Set<String> getSupportedFilterNames() {
        return Collections.unmodifiableSet(factories.keySet());
    }

    public boolean isSupported(String filterName) {
        return factories.containsKey(filterName);
    }
}