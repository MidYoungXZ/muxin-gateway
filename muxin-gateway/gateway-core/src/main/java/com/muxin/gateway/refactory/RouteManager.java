package com.muxin.gateway.refactory;

import java.util.List;

/**
 * 路由管理器接口
 *
 * @author muxin
 */
public interface RouteManager {
    
    /**
     * 添加路由
     */
    void addRoute(UniversalRoute route);
    
    /**
     * 删除路由
     */
    void removeRoute(String routeId);
    
    /**
     * 获取路由
     */
    UniversalRoute getRoute(String routeId);
    
    /**
     * 获取所有路由
     */
    List<UniversalRoute> getAllRoutes();
    
    /**
     * 匹配路由
     */
    UniversalRoute matchRoute(UniversalRequestContext context);
} 