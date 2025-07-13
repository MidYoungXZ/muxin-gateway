package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.common.Repository;

/**
 * 路由管理器接口
 *
 * @author muxin
 */
public interface RouteManager extends Repository<String, Route>, LifeCycle {

    /**
     * 匹配路由
     */
    Route matchRoute(RequestContext context);

} 