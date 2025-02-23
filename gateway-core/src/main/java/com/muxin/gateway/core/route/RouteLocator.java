package com.muxin.gateway.core.route;

import com.muxin.gateway.core.LifeCycle;

import java.util.List;

/**
 * 定义了一个路由定位器接口，继承自 {@link LifeCycle}。
 * 该接口用于根据请求路径获取匹配的路由规则，并提供了启动和关闭的方法。
 *
 * @author Administrator
 * @date 2024/11/21 09:58
 */
public interface RouteLocator extends LifeCycle {

    /**
     * 根据请求路径获取匹配的路由规则列表。
     *
     * @param path 请求路径
     * @return 匹配的路由规则列表
     */
    List<RouteRule> getRoutes(String path);

    /**
     * 启动路由定位器。
     * <p>
     * 实现类可以根据需要重写此方法以执行启动时的初始化逻辑。
     */
    @Override
    default void start() {}

    /**
     * 关闭路由定位器。
     * <p>
     * 实现类可以根据需要重写此方法以执行关闭时的清理逻辑。
     */
    @Override
    default void shutdown() {}
}
