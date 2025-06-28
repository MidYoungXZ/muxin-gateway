package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;

/**
 * 路由管理器接口
 *
 * @author muxin
 */
public interface RouteManager extends Repository<String, UniversalRoute>, LifeCycle {

    /**
     * 匹配路由
     */
    UniversalRoute matchRoute(UniversalRequestContext context);
} 