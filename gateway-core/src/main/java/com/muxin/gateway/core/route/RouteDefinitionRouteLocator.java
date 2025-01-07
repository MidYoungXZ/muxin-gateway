package com.muxin.gateway.core.route;

import com.muxin.gateway.core.factory.FilterFactory;
import com.muxin.gateway.core.factory.PredicateFactory;
import com.muxin.gateway.core.filter.FilterDefinition;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import com.muxin.gateway.core.route.path.AntPathMatcher;
import com.muxin.gateway.core.route.path.PathMatcher;
import com.muxin.gateway.core.route.path.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由定位器实现，同时作为RouteDefinition和RouteLocator的桥梁
 */
@Slf4j
public class RouteDefinitionRouteLocator implements RouteLocator {
    /**
     * 路由定义定位器
     */
    private final RouteDefinitionLocator routeDefinitionLocator;

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

    private final FilterFactory filterFactory;
    private final PredicateFactory predicateFactory;

    public RouteDefinitionRouteLocator(
            RouteDefinitionLocator routeDefinitionLocator,
            FilterFactory filterFactory,
            PredicateFactory predicateFactory) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.filterFactory = filterFactory;
        this.predicateFactory = predicateFactory;
    }

    /**
     * 根据路由获取路由信息
     *
     * @param path 请求路径
     * @return 路由规则集合
     */
    @Override
    public List<RouteRule> getRoutes(String path) {
        if (path == null || path.isEmpty()) {
            return Collections.emptyList();
        }

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

        // 合并多个组的路由规则
        List<RouteRule> mergedRoutes = new ArrayList<>(groups.size() * 2);
        for (RouteRuleGroup rg : groups) {
            mergedRoutes.addAll(rg.getRoutes());
        }
        return mergedRoutes;
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
        if (route == null || route.getId() == null) {
            log.warn("Invalid route: {}", route);
            return;
        }

        if (allRoutes.containsKey(route.getId())) {
            // 是一个已经存在的api的更新动作，其path可能已经改变，要先根据id删除之
            removeRouteById(route.getId());
        }

        //添加到全量routeRule
        this.allRoutes.put(route.getId(), route);
        String normalizePath = PathUtil.normalize(route.getUri().getPath());
        
        try {
            addRouteToGroup(route, normalizePath);
        } catch (Exception e) {
            // 如果添加到组失败，需要回滚
            allRoutes.remove(route.getId());
            log.error("Failed to add route to group: {}", route, e);
            throw e;
        }
    }

    private void addRouteToGroup(RouteRule route, String normalizePath) {
        RouteRuleGroup targetGroup;
        if (pathMatcher.isPattern(normalizePath)) {
            //带正则表达式的routeRule
            String prefix = PathUtil.constantPrefix(normalizePath);
            targetGroup = patternPathRoutes.computeIfAbsent(prefix, k -> new RouteRuleGroup());
        } else {
            //常量路径的routeRule
            targetGroup = constantPathRoutes.computeIfAbsent(normalizePath, k -> new RouteRuleGroup());
        }
        targetGroup.addRoute(route);
    }

    protected synchronized RouteRule removeRouteById(String routeId) {
        if (routeId == null || !allRoutes.containsKey(routeId)) {
            return null;
        }

        RouteRule removedRoute = allRoutes.remove(routeId);
        String normalizePath = PathUtil.normalize(removedRoute.getUri().getPath());

        if (pathMatcher.isPattern(normalizePath)) {
            removeFromPatternGroup(routeId, normalizePath);
        } else {
            removeFromConstantGroup(routeId, normalizePath);
        }

        return removedRoute;
    }

    private void removeFromPatternGroup(String routeId, String normalizePath) {
        String prefix = PathUtil.constantPrefix(normalizePath);
        RouteRuleGroup targetGroup = patternPathRoutes.get(prefix);
        if (Objects.nonNull(targetGroup)) {
            targetGroup.removeRouteById(routeId);
            if (targetGroup.isEmpty()) {
                patternPathRoutes.remove(prefix);
            }
        }
    }

    private void removeFromConstantGroup(String routeId, String normalizePath) {
        RouteRuleGroup targetGroup = constantPathRoutes.get(normalizePath);
        if (Objects.nonNull(targetGroup)) {
            targetGroup.removeRouteById(routeId);
            if (targetGroup.isEmpty()) {
                constantPathRoutes.remove(normalizePath);
            }
        }
    }

    /**
     * 初始化路由规则
     */
    @Override
    public void init() {
        // 从routeDefinitionLocator加载路由定义并初始化
        if (routeDefinitionLocator != null) {
            routeDefinitionLocator.getRouteDefinitions().forEach(definition -> {
                RouteRule routeRule = convertToRouteRule(definition);
                addRoute(routeRule);
            });
        }
    }

    /**
     * 将RouteDefinition转换为RouteRule
     */
    private RouteRule convertToRouteRule(RouteDefinition definition) {
        // 转换过滤器定义
        List<RouteRuleFilter> filters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(definition.getFilters())) {
            for (FilterDefinition filterDef : definition.getFilters()) {
                RouteRuleFilter filter = convertToFilter(filterDef);
                if (filter != null) {
                    filters.add(filter);
                }
            }
        }

        // 转换断言定义
        RoutePredicate predicate = null;
        if (!CollectionUtils.isEmpty(definition.getPredicates())) {
            predicate = convertToPredicate(definition.getPredicates());
        }

        // 构建RouteRule
        return RouteRule.builder()
                .id(definition.getId())
                .uri(definition.getUri())
                .order(definition.getOrder())
                .routeRuleFilters(filters)
                .predicate(predicate != null ? predicate : exchange -> true)
                .metadata(definition.getMetadata())
                .build();
    }

    /**
     * 转换过滤器定义为具体的过滤器实例
     */
    private RouteRuleFilter convertToFilter(FilterDefinition filterDef) {
        try {
            // 这里需要通过FilterFactory来创建具体的过滤器实例
            return filterFactory.create(filterDef.getName(), filterDef.getArgs());
        } catch (Exception e) {
            log.error("Failed to create filter from definition: {}", filterDef, e);
            return null;
        }
    }

    /**
     * 转换断言定义为断言实例
     */
    private RoutePredicate convertToPredicate(List<PredicateDefinition> predicates) {
        List<RoutePredicate> routePredicates = new ArrayList<>();
        
        for (PredicateDefinition predicate : predicates) {
            try {
                RoutePredicate routePredicate = predicateFactory.create(predicate.getName(), predicate.getArgs());
                if (routePredicate != null) {
                    routePredicates.add(routePredicate);
                }
            } catch (Exception e) {
                log.error("Failed to create predicate from definition: {}", predicate, e);
            }
        }

        // 组合所有断言
        return exchange -> routePredicates.stream().allMatch(p -> p.test(exchange));
    }

    private static class RouteRuleGroup {
        private volatile boolean changed = false;
        private final Map<String, RouteRule> routes = new HashMap<>(4);
        private List<RouteRule> cachedRoutes = Collections.emptyList();

        List<RouteRule> getRoutes() {
            if (changed) {
                cachedRoutes = new ArrayList<>(routes.values());
                changed = false;
            }
            return cachedRoutes;
        }

        void addRoute(RouteRule route) {
            routes.put(route.getId(), route);
            changed = true;
        }

        void removeRouteById(String id) {
            changed = Objects.nonNull(routes.remove(id));
        }

        boolean isEmpty() {
            return routes.isEmpty();
        }
    }
}
