package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;
import com.muxin.gateway.core.plus.monitor.Monitorable;

/**
 * 路由管理器接口
 *
 * @author muxin
 */
public interface RouteManager extends Repository<String, Route>, Monitorable, LifeCycle {

    /**
     * 匹配路由
     */
    Route matchRoute(RequestContext context);
} 