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
    private final Map<String, RouteRuleGroup> constantPathRoutes = new ConcurrentHashMap<>(8);
    /**
     * Route配置的path都不是常量，含有pattern，key是最长的常量前缀
     * 比如一个Route的path是/foo/bar/{name}/info,那么它应该在key为/foo/bar的group中
     */
    private final Map<String, RouteRuleGroup> patternPathRoutes = new ConcurrentHashMap<>(8);

    /**
     * AntPathMatcher
     */
    private final PathMatcher pathMatcher = AntPathMatcher.getDefaultInstance();


    /**
     * 根据路由获取路由信息
     *
     * @param path 请求路径
     * @return 路由规则集合
     */
    @Override
    public List<RouteRule> getRoutes(String path) {
        //先解决常量路径
        String normalizePath = PathUtil.normalize(path);
        RouteRuleGroup routeRuleGroup = constantPathRoutes.get(normalizePath);
        List<RouteRuleGroup> groups = findInPatternPathRoutes(normalizePath);
        if (Objects.nonNull(routeRuleGroup)) {
            groups.add(0, routeRuleGroup);
        }
        if (groups.isEmpty()) {
            return Collections.emptyList();
        } else if (groups.size() == 1) {
            return groups.get(0).getRoutes();
        }
        List<RouteRule> list = new ArrayList<>(groups.size());
        for (RouteRuleGroup rg : groups) {
            list.addAll(rg.getRoutes());
        }
        return list;
    }

    private List<RouteRuleGroup> findInPatternPathRoutes(String normalizePath) {
        String prefix = PathUtil.removeLast(normalizePath);
        List<RouteRuleGroup> groups = new LinkedList<>();
        while (!prefix.isEmpty()) {
            RouteRuleGroup routeRuleGroup = patternPathRoutes.get(prefix);
            if (Objects.nonNull(routeRuleGroup)) {
                groups.add(routeRuleGroup);
            }
            prefix = PathUtil.removeLast(prefix);
        }
        return groups;
    }


    protected synchronized void addRoute(RouteRule route) {
        if (allRoutes.containsKey(route.getId())) {
            // 是一个已经存在的api的更新动作，其path可能已经改变，要先根据id删除之
            removeRouteById(route.getId());
        }
        //添加到全量routeRule
        this.allRoutes.put(route.getId(), route);
        String normalizePath = PathUtil.normalize(route.getUri().getPath());
        RouteRuleGroup targetGroup;
        if (pathMatcher.isPattern(normalizePath)) {
            //带正则表达式的routeRule
            String prefix = PathUtil.constantPrefix(normalizePath);
            targetGroup = patternPathRoutes.get(prefix);
            if (Objects.isNull(targetGroup)) {
                targetGroup = new RouteRuleGroup();
                patternPathRoutes.put(prefix, targetGroup);
            }
        } else {
            //常量路径的routeRule
            targetGroup = constantPathRoutes.get(normalizePath);
            if (Objects.isNull(targetGroup)) {
                targetGroup = new RouteRuleGroup();
                constantPathRoutes.put(normalizePath, targetGroup);
            }
        }
        targetGroup.addRoute(route);
    }

    protected synchronized RouteRule removeRouteById(String routeId) {
        if (!allRoutes.containsKey(routeId)) {
            return null;
        }
        RouteRule removedRoute = allRoutes.remove(routeId);
        String normalizePath = PathUtil.normalize(removedRoute.getUri().getPath());

        RouteRuleGroup targetGroup;
        if (pathMatcher.isPattern(normalizePath)) {
            String prefix = PathUtil.constantPrefix(normalizePath);
            targetGroup = patternPathRoutes.get(prefix);
            if (Objects.nonNull(targetGroup)) {
                targetGroup.removeRouteById(routeId);
                if (targetGroup.isEmpty()) {
                    patternPathRoutes.remove(prefix);
                }
            }
        } else {
            targetGroup = constantPathRoutes.get(normalizePath);
            if (Objects.nonNull(targetGroup)) {
                targetGroup.removeRouteById(routeId);
                if (targetGroup.isEmpty()) {
                    constantPathRoutes.remove(normalizePath);
                }
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

    private static class RouteRuleGroup {

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
