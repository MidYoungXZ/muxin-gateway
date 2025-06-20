package com.muxin.gateway.refactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 简单路由管理器实现
 *
 * @author muxin
 */
public class SimpleRouteManager implements RouteManager {
    
    private final Map<String, UniversalRoute> routes = new ConcurrentHashMap<>();
    
    @Override
    public void addRoute(UniversalRoute route) {
        if (route == null || route.getId() == null) {
            throw new IllegalArgumentException("Route and route ID cannot be null");
        }
        
        routes.put(route.getId(), route);
        System.out.println("Added route: " + route.getId() + " - " + route.getName());
    }
    
    @Override
    public void removeRoute(String routeId) {
        if (routeId == null) {
            return;
        }
        
        UniversalRoute removed = routes.remove(routeId);
        if (removed != null) {
            System.out.println("Removed route: " + routeId + " - " + removed.getName());
        }
    }
    
    @Override
    public UniversalRoute getRoute(String routeId) {
        return routes.get(routeId);
    }
    
    @Override
    public List<UniversalRoute> getAllRoutes() {
        return new ArrayList<>(routes.values());
    }
    
    @Override
    public UniversalRoute matchRoute(UniversalRequestContext context) {
        if (context == null) {
            return null;
        }
        
        // 按优先级排序（order值越小优先级越高）
        List<UniversalRoute> sortedRoutes = routes.values().stream()
                .filter(UniversalRoute::isEnabled)
                .sorted(Comparator.comparingInt(UniversalRoute::getOrder))
                .collect(Collectors.toList());
        
        // 遍历路由进行匹配
        for (UniversalRoute route : sortedRoutes) {
            try {
                if (route.matches(context)) {
                    System.out.println("Matched route: " + route.getId() + " for request");
                    return route;
                }
            } catch (Exception e) {
                System.err.println("Error matching route " + route.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("No route matched for request");
        return null;
    }
    
    /**
     * 根据协议过滤路由
     */
    public List<UniversalRoute> getRoutesByProtocol(Protocol protocol) {
        if (protocol == null) {
            return Collections.emptyList();
        }
        
        return routes.values().stream()
                .filter(route -> route.getSupportedProtocols().contains(protocol))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取路由统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", routes.size());
        stats.put("enabledRoutes", routes.values().stream()
                .mapToInt(route -> route.isEnabled() ? 1 : 0)
                .sum());
        
        // 按协议分组统计
        Map<String, Long> protocolStats = routes.values().stream()
                .flatMap(route -> route.getSupportedProtocols().stream())
                .collect(Collectors.groupingBy(
                        Protocol::getName,
                        Collectors.counting()
                ));
        stats.put("protocolDistribution", protocolStats);
        
        return stats;
    }
} 