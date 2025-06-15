package com.muxin.gateway.admin.service;

import com.muxin.gateway.core.route.RouteDefinition;

import java.util.List;

/**
 * 路由管理服务接口
 */
public interface RouteService {

    /**
     * 获取所有路由定义
     *
     * @return 路由定义列表
     */
    List<RouteDefinition> getAllRoutes();

    /**
     * 根据ID获取路由定义
     *
     * @param id 路由ID
     * @return 路由定义，不存在时返回null
     */
    RouteDefinition getRouteById(String id);

    /**
     * 创建或更新路由
     *
     * @param routeDefinition 路由定义
     * @return 创建或更新的路由定义
     */
    RouteDefinition saveRoute(RouteDefinition routeDefinition);

    /**
     * 删除路由
     *
     * @param id 路由ID
     * @return 是否删除成功
     */
    boolean deleteRoute(String id);
} 