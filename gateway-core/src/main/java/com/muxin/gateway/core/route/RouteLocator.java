package com.muxin.gateway.core.route;

import com.muxin.gateway.core.LifeCycle;

import java.util.List;

/**
 * 路由定位器接口
 */
public interface RouteLocator extends LifeCycle {

    /**
     * 根据路径获取路由列表
     */
    List<RouteRule> getRoutes(String path);

    /**
     * 获取所有路由列表
     */
    List<RouteRule> getAllRoutes();

    /**
     * 启动
     */
    default void start() {}

    /**
     * 关闭
     */
    default void shutdown() {}
}
