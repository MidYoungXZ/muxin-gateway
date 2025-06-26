package com.muxin.gateway.refactory.route;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.LifeCycle;

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