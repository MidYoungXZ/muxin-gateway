package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.filter.Filter;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.predicate.Predicate;
import com.muxin.gateway.core.plus.predicate.PredicateFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 路由配置转换器
 * 将YAML配置转换为Route对象
 *
 * @author muxin
 */
@Slf4j
public class RouteConfigConverter {
    
    private final PredicateFactory predicateFactory;
    
    public RouteConfigConverter(PredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }
    
    /**
     * 将EnhancedRouteConfig转换为EnhancedRoute
     */
    public EnhancedRoute convertToRoute(EnhancedRouteConfig config) {
        try {
            // 验证配置
            config.validate();
            
            // 转换协议
            Protocol inboundProtocol = config.getInboundProtocol().toProtocol();
            
            // 转换断言
            List<Predicate> predicates = convertPredicates(config.getPredicates());
            
            // 转换过滤器
            List<Filter> filters = convertFilters(config.getFilters());
            
            // 转换超时配置
            TimeoutConfig timeouts = convertTimeouts(config.getTimeouts());
            
            return EnhancedRoute.builder()
                    .id(config.getId())
                    .name(config.getName())
                    .description(config.getDescription())
                    .order(config.getOrder())
                    .enabled(config.isEnabled())
                    .inboundProtocol(inboundProtocol)
                    .predicates(predicates)
                    .filters(filters)
                    .target(config.getTarget())
                    .timeouts(timeouts)
                    .metadata(config.getMetadata())
                    .build();
                    
        } catch (Exception e) {
            log.error("转换路由配置失败: {}", config.getId(), e);
            throw new IllegalArgumentException("转换路由配置失败: " + config.getId(), e);
        }
    }
    
    /**
     * 批量转换路由配置
     */
    public List<EnhancedRoute> convertToRoutes(List<EnhancedRouteConfig> configs) {
        List<EnhancedRoute> routes = new ArrayList<>();
        
        for (EnhancedRouteConfig config : configs) {
            try {
                EnhancedRoute route = convertToRoute(config);
                routes.add(route);
                log.debug("成功转换路由: {}", config.getId());
            } catch (Exception e) {
                log.error("转换路由失败，跳过: {}", config.getId(), e);
                // 继续处理其他路由，不中断整个转换过程
            }
        }
        
        log.info("路由转换完成，成功: {}, 失败: {}", routes.size(), configs.size() - routes.size());
        return routes;
    }
    
    /**
     * 转换断言配置
     */
    private List<Predicate> convertPredicates(List<PredicateConfig> predicateConfigs) {
        if (predicateConfigs == null || predicateConfigs.isEmpty()) {
            return List.of();
        }
        
        List<Predicate> predicates = new ArrayList<>();
        
        for (PredicateConfig config : predicateConfigs) {
            try {
                                 Predicate predicate = predicateFactory.createFromConfig(config.getType(), config.getConfig());
                predicates.add(predicate);
                log.debug("成功创建断言: {}", config.getType());
            } catch (Exception e) {
                log.error("创建断言失败: {}", config.getType(), e);
                throw new IllegalArgumentException("创建断言失败: " + config.getType(), e);
            }
        }
        
        return predicates;
    }
    
    /**
     * 转换过滤器配置
     */
    private List<Filter> convertFilters(List<FilterConfig> filterConfigs) {
        if (filterConfigs == null || filterConfigs.isEmpty()) {
            return List.of();
        }
        
        List<Filter> filters = new ArrayList<>();
        
        for (FilterConfig config : filterConfigs) {
            if (!config.isEnabled()) {
                log.debug("跳过已禁用的过滤器: {}", config.getType());
                continue;
            }
            
                         // TODO: 实现过滤器工厂创建逻辑
             log.debug("跳过过滤器创建: {} (待实现)", config.getType());
        }
        
        // 按order排序
        filters.sort((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()));
        
        return filters;
    }
    
    /**
     * 转换超时配置
     */
    private TimeoutConfig convertTimeouts(TimeoutConfig config) {
        if (config == null) {
            return TimeoutConfig.defaultConfig();
        }
        return config;
    }
    
    /**
     * 解析时间字符串为Duration
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 支持简单的时间格式解析，如: "30s", "5m", "1h"
            String trimmed = durationStr.trim().toLowerCase();
            
            if (trimmed.endsWith("s")) {
                long seconds = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofSeconds(seconds);
            } else if (trimmed.endsWith("m")) {
                long minutes = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofMinutes(minutes);
            } else if (trimmed.endsWith("h")) {
                long hours = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofHours(hours);
            } else if (trimmed.endsWith("ms")) {
                long millis = Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
                return Duration.ofMillis(millis);
            } else {
                // 默认按秒解析
                long seconds = Long.parseLong(trimmed);
                return Duration.ofSeconds(seconds);
            }
        } catch (NumberFormatException e) {
            log.warn("无法解析时间格式: {}, 使用默认值", durationStr);
            return null;
        }
    }
} 