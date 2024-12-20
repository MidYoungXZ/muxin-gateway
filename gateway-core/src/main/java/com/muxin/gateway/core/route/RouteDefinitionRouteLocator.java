package com.muxin.gateway.core.route;


import com.muxin.gateway.core.route.path.AntPathMatcher;
import com.muxin.gateway.core.route.path.PathMatcher;
import com.muxin.gateway.core.route.path.PathUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * RouteLocator实现同时时RouteDefinition和RouteLocator的桥梁
 */
public class RouteDefinitionRouteLocator implements RouteLocator {
    /**
     * 路由定义定位器
     */
    private RouteDefinitionLocator routeDefinitionLocator;

    /**
     * 所有路由规则
     */
    private final Map<String, RouteRule> allRoutes = new ConcurrentHashMap<>(8);

    /**
     * Route配置的path都是常量，没有pattern，key就是normalize后的path
     */
    private final Map<String, RouteGroup> constantPathRoutes = new ConcurrentHashMap<>(8);


    private final PathMatcher pathMatcher = AntPathMatcher.getDefaultInstance();


    /**
     * 根据路由获取路由信息
     *
     * @param path 请求路径
     * @return 路由规则集合
     */
    @Override
    public List<RouteRule> getRoutes(String path) {
        //先解决常量路径   todo 完善更复杂的判断 比如通过path传参的url
        String normalizePath = PathUtil.normalize(path);
        RouteGroup routeGroup = constantPathRoutes.get(normalizePath);
        if (routeGroup == null) {
            return new ArrayList<>(allRoutes.values());
        }
        return routeGroup.getRoutes();
    }


    protected synchronized void addRoute(RouteRule route) {
        if (allRoutes.containsKey(route.getId())) {
            // 是一个已经存在的api的更新动作，其path可能已经改变，要先根据id删除之
            removeRouteById(route.getId());
        }
        this.allRoutes.put(route.getId(), route);
        String normalizePath = PathUtil.normalize(route.getUri().getPath());
        RouteGroup targetGroup;
        targetGroup = constantPathRoutes.get(normalizePath);
        if (Objects.isNull(targetGroup)) {
            targetGroup = new RouteGroup();
            constantPathRoutes.put(normalizePath, targetGroup);
        }
        targetGroup.addRoute(route);
    }

    protected synchronized RouteRule removeRouteById(String routeId) {
        if (!allRoutes.containsKey(routeId)) {
            return null;
        }
        RouteRule removedRoute = allRoutes.remove(routeId);
        String normalizePath = PathUtil.normalize(removedRoute.getUri().getPath());
        RouteGroup targetGroup;
        targetGroup = constantPathRoutes.get(normalizePath);
        if (Objects.nonNull(targetGroup)) {
            targetGroup.removeRouteById(routeId);
            if (targetGroup.isEmpty()) {
                constantPathRoutes.remove(normalizePath);
            }
        }
        return removedRoute;
    }

    /**
     * 初始化
     */
    @Override
    public void init() {

    }

    private static class RouteGroup {

        volatile boolean changed = false;

        final Map<String, RouteRule> routes = new HashMap<>(4);

        List<RouteRule> empty = Collections.emptyList();

        List<RouteRule> getRoutes() {
            if (changed) {
                this.empty = new ArrayList<>(routes.values());
                changed = false;
            }
            return empty;
        }

        void addRoute(RouteRule route) {
            routes.put(route.getId(), route);
            changed = true;
        }

        void removeRouteById(String id) {
            changed = Objects.nonNull(this.routes.remove(id));
        }

        boolean isEmpty() {
            return this.routes.isEmpty();
        }

    }

}
