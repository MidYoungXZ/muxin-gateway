package com.muxin.gateway.core.route;

import com.muxin.gateway.core.LifeCycle;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 09:58
 */
public interface RouteLocator  extends LifeCycle {

    List<RouteRule> getRoutes(String path);

    /**
     * 启动
     */
    default void start(){}

    /**
     * 关闭
     */
    default void shutdown(){}

}
