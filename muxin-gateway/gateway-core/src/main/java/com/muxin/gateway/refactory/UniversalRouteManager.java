package com.muxin.gateway.refactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通用路由管理器实现
 *
 * @author muxin
 */
public class UniversalRouteManager implements RouteManager {
    
    private final List<UniversalRoute> routes = new CopyOnWriteArrayList<>();
    
    @Override
    public void addRoute(UniversalRoute route) {
        routes.add(route);
        // 按优先级排序
        routes.sort(Comparator.comparingInt(UniversalRoute::getOrder));
        System.out.println("Added route: " + route.getId() + " - " + route.getName());
    }
    
    @Override
    public void removeRoute(String routeId) {
        routes.removeIf(route -> route.getId().equals(routeId));
        System.out.println("Removed route: " + routeId);
    }
    
    @Override
    public UniversalRoute matchRoute(UniversalRequestContext context) {
        for (UniversalRoute route : routes) {
            if (route.isEnabled() && route.matches(context)) {
                System.out.println("Matched route: " + route.getId() + " for request");
                return route;
            }
        }
        return null;
    }
    
    @Override
    public List<UniversalRoute> getAllRoutes() {
        return new ArrayList<>(routes);
    }
    
    @Override
    public UniversalRoute getRoute(String routeId) {
        return routes.stream()
            .filter(route -> route.getId().equals(routeId))
            .findFirst()
            .orElse(null);
    }
} 