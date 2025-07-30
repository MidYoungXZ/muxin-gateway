package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.FilterDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.plus.route.predicate.PredicateDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全局路由配置
 * 提供默认配置，路由可以继承这些配置
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalRouteConfig {
    
    /**
     * 全局默认过滤器（所有路由都会继承）
     */
    @Builder.Default
    private List<FilterDefinition> globalFilters = new ArrayList<>();
    
    /**
     * 全局默认断言器（可选，通常路由会有自己的断言）
     */
    @Builder.Default
    private List<PredicateDefinition> globalPredicates = new ArrayList<>();
    
    /**
     * 默认负载均衡配置
     */
    @Builder.Default
    private LoadBalanceDefinition defaultLoadBalance = LoadBalanceDefinition.builder()
            .strategy("ROUND_ROBIN")
            .build();
    
    /**
     * 默认超时配置
     */
    @Builder.Default
    private TimeoutConfig defaultTimeouts = TimeoutConfig.defaultConfig();
    
    /**
     * 全局元数据
     */
    private Map<String, Object> globalMetadata;
    
    /**
     * 是否启用全局过滤器
     */
    @Builder.Default
    private boolean enableGlobalFilters = true;
    
    /**
     * 是否启用全局断言器
     */
    @Builder.Default
    private boolean enableGlobalPredicates = false;
    
    /**
     * 合并路由配置（路由配置优先，全局配置作为默认值）
     */
    public RouteDefinition mergeRouteDefinition(RouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            throw new IllegalArgumentException("路由配置不能为空");
        }
        
        // 创建合并后的配置
        RouteDefinition.RouteDefinitionBuilder builder = RouteDefinition.builder()
                .id(routeDefinition.getId())
                .name(routeDefinition.getName())
                .description(routeDefinition.getDescription())
                .order(routeDefinition.getOrder())
                .enabled(routeDefinition.isEnabled())
                .supportProtocol(routeDefinition.getSupportProtocol())
                .service(routeDefinition.getService());
        
        // 合并过滤器：全局过滤器 + 路由过滤器
        List<FilterDefinition> mergedFilters = new ArrayList<>();
        if (enableGlobalFilters && globalFilters != null) {
            mergedFilters.addAll(globalFilters);
        }
        if (routeDefinition.getFilters() != null) {
            mergedFilters.addAll(routeDefinition.getFilters());
        }
        builder.filters(mergedFilters);
        
        // 合并断言器：全局断言器 + 路由断言器
        List<PredicateDefinition> mergedPredicates = new ArrayList<>();
        if (enableGlobalPredicates && globalPredicates != null) {
            mergedPredicates.addAll(globalPredicates);
        }
        if (routeDefinition.getPredicates() != null) {
            mergedPredicates.addAll(routeDefinition.getPredicates());
        }
        builder.predicates(mergedPredicates);
        
        // 合并超时配置：路由配置优先
        TimeoutConfig timeouts = routeDefinition.getTimeouts() != null ? 
                routeDefinition.getTimeouts() : defaultTimeouts;
        builder.timeouts(timeouts);
        
        // 合并负载均衡配置：路由级别的负载均衡优先，如果没有则使用默认配置
        LoadBalanceDefinition loadBalance = routeDefinition.getLoadBalance() != null ? 
                routeDefinition.getLoadBalance() : defaultLoadBalance;
        builder.loadBalance(loadBalance);
        
        // 合并元数据
        Map<String, Object> mergedMetadata = routeDefinition.getMetadata();
        if (globalMetadata != null) {
            if (mergedMetadata == null) {
                mergedMetadata = new java.util.HashMap<>(globalMetadata);
            } else {
                // 路由元数据优先，全局元数据作为默认值
                Map<String, Object> finalMetadata = new java.util.HashMap<>(globalMetadata);
                finalMetadata.putAll(mergedMetadata);
                mergedMetadata = finalMetadata;
            }
        }
        builder.metadata(mergedMetadata);
        
        return builder.build();
    }
    
    /**
     * 添加全局过滤器
     */
    public void addGlobalFilter(FilterDefinition filter) {
        if (globalFilters == null) {
            globalFilters = new ArrayList<>();
        }
        globalFilters.add(filter);
    }
    
    /**
     * 添加全局断言器
     */
    public void addGlobalPredicate(PredicateDefinition predicate) {
        if (globalPredicates == null) {
            globalPredicates = new ArrayList<>();
        }
        globalPredicates.add(predicate);
    }
    
    /**
     * 创建默认全局配置
     */
    public static GlobalRouteConfig defaultConfig() {
        return GlobalRouteConfig.builder()
                .enableGlobalFilters(true)
                .enableGlobalPredicates(false)
                .build();
    }
} 