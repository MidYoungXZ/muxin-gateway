package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.FilterDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;

import com.muxin.gateway.core.plus.route.predicate.PredicateDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Slf4j
public class GlobalRouteConfig {
    
    private boolean enableGlobalFilters = true;
    
    private boolean enableGlobalPredicates = true;
    
    private List<FilterDefinition> globalFilters = new ArrayList<>();
    
    private List<PredicateDefinition> globalPredicates = new ArrayList<>();
    
    private Map<String, Object> globalMetadata = new HashMap<>();
    
    private TimeoutConfig defaultTimeouts;
    
    public TimeoutConfig getDefaultTimeouts() {
        return defaultTimeouts;
    }
    
    private LoadBalanceDefinition defaultLoadBalance;
    
    public LoadBalanceDefinition getDefaultLoadBalance() {
        return defaultLoadBalance;
    }
    
    public RouteDefinition merge(RouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            throw new IllegalArgumentException("路由定义不能为空");
        }
        
        List<FilterDefinition> mergedFilters = new ArrayList<>();
        if (enableGlobalFilters && globalFilters != null) {
            mergedFilters.addAll(globalFilters);
        }
        if (routeDefinition.getFilters() != null) {
            mergedFilters.addAll(routeDefinition.getFilters());
        }
        
        List<PredicateDefinition> mergedPredicates = new ArrayList<>();
        if (enableGlobalPredicates && globalPredicates != null) {
            mergedPredicates.addAll(globalPredicates);
        }
        if (routeDefinition.getPredicates() != null) {
            mergedPredicates.addAll(routeDefinition.getPredicates());
        }
        
        TimeoutConfig timeouts = routeDefinition.getTimeouts() != null ?
                routeDefinition.getTimeouts() : defaultTimeouts;
        
        LoadBalanceDefinition loadBalance = routeDefinition.getLoadBalance() != null ?
                routeDefinition.getLoadBalance() : defaultLoadBalance;
        
        Map<String, Object> mergedMetadata = routeDefinition.getMetadata();
        if (globalMetadata != null) {
            if (mergedMetadata == null) {
                mergedMetadata = new java.util.HashMap<>(globalMetadata);
            } else {
                Map<String, Object> finalMetadata = new java.util.HashMap<>(globalMetadata);
                finalMetadata.putAll(mergedMetadata);
                mergedMetadata = finalMetadata;
            }
        }
        
        RouteDefinition mergedDefinition = new RouteDefinition();
        mergedDefinition.setId(routeDefinition.getId());
        mergedDefinition.setName(routeDefinition.getName());
        mergedDefinition.setDescription(routeDefinition.getDescription());
        mergedDefinition.setOrder(routeDefinition.getOrder());
        mergedDefinition.setEnabled(routeDefinition.isEnabled());
        mergedDefinition.setProtocol(routeDefinition.getProtocol());
        mergedDefinition.setServiceRef(routeDefinition.getServiceRef());
        mergedDefinition.setPredicates(mergedPredicates);
        mergedDefinition.setFilters(mergedFilters);
        mergedDefinition.setLoadBalance(loadBalance);
        mergedDefinition.setTimeouts(timeouts);
        mergedDefinition.setMetadata(mergedMetadata);
        
        return mergedDefinition;
    }
    
    public void addGlobalFilter(FilterDefinition filter) {
        if (globalFilters == null) {
            globalFilters = new ArrayList<>();
        }
        globalFilters.add(filter);
    }
    
    public void addGlobalPredicate(PredicateDefinition predicate) {
        if (globalPredicates == null) {
            globalPredicates = new ArrayList<>();
        }
        globalPredicates.add(predicate);
    }
    
    public static GlobalRouteConfig defaultConfig() {
        return GlobalRouteConfig.builder()
                .enableGlobalFilters(true)
                .enableGlobalPredicates(false)
                .build();
    }
}