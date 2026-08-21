package com.muxin.gateway.core.route;

import com.muxin.gateway.core.common.LifeCycle;
import com.muxin.gateway.core.common.Repository;

/**
 * 路由管理器接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RouteManager extends Repository<String, Route>, LifeCycle {

    /**
     * 匹配路由
     */
    Route matchRoute(RequestContext context);

    /**
     * 设置默认路由
     */
    void setDefaultRoute(Route route);

    /**
     * 获取默认路由
     */
    Route getDefaultRoute();

    /**
     * 清空所有路由
     */
    void clear();

    void replaceAll(java.util.List<Route> routes);

}
