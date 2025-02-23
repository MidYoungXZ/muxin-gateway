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
 * 路由定位器实现，同时作为RouteDefinition和RouteLocator的桥梁。
 * 该类负责从路由定义仓库中加载路由定义，并将其转换为路由规则，以便进行路由匹配和处理。
 *
 * @author Administrator
 * @date 2024/11/21 11:10
 */
@Slf4j
public class RouteDefinitionRouteLocator implements RouteLocator {

    /**
     * 路由定义仓库，用于存储和管理路由定义。
     */
    private final RouteDefinitionRepository routeDefinitionRepository;

    /**
     * 所有路由规则的映射，键为路由ID，值为路由规则。
     */
    private final Map<String, RouteRule> allRoutes = new ConcurrentHashMap<>(8);

    /**
     * 常量路径的路由规则组，键为标准化后的路径，值为路由规则组。
     */
    private final Map<String, RouteRuleGroup> constantPathRoutes = new ConcurrentHashMap<>(8);

    /**
     * 模式路径的路由规则组，键为最长的常量前缀，值为路由规则组。
     */
    private final Map<String, RouteRuleGroup> patternPathRoutes = new ConcurrentHashMap<>(8);

    /**
     * AntPathMatcher，用于路径匹配。
     */
    private final PathMatcher pathMatcher = AntPathMatcher.getDefaultInstance();

    /**
     * 过滤器工厂映射，键为过滤器名称，值为过滤器工厂。
     */
    private final Map<String, FilterFactory> filterFactoryMap;

    /**
     * 断言工厂映射，键为断言名称，值为断言工厂。
     */
    private final Map<String, PredicateFactory> predicateFactoryMap;

    /**
     * 构造函数，用于初始化路由定位器。
     *
     * @param routeDefinitionRepository 路由定义仓库
     * @param filterFactoryMap 过滤器工厂映射
     * @param predicateFactoryMap 断言工厂映射
     */
    public RouteDefinitionRouteLocator(RouteDefinitionRepository routeDefinitionRepository, Map<String, FilterFactory> filterFactoryMap, Map<String, PredicateFactory> predicateFactoryMap) {
        this.routeDefinitionRepository = routeDefinitionRepository;
        this.filterFactoryMap = filterFactoryMap;
        this.predicateFactoryMap = predicateFactoryMap;
    }

    /**
     * 根据请求路径获取匹配的路由规则。
     *
     * @param path 请求路径
     * @return 匹配的路由规则集合
     */
    @Override
    public List<RouteRule> getRoutes(String path) {
        if (path == null || path.isEmpty()) {
            return Collections.emptyList();
        }
        // 先解决常量路径
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

    /**
     * 查找匹配模式路径的路由规则组。
     *
     * @param normalizePath 标准化后的路径
     * @return 匹配的路由规则组列表
     */
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

    /**
     * 添加路由规则。
     *
     * @param route 路由规则
     */
    protected synchronized void addRoute(RouteRule route) {
        if (route == null || route.getId() == null) {
            log.warn("Invalid route: {}", route);
            return;
        }

        if (allRoutes.containsKey(route.getId())) {
            // 是一个已经存在的路由的更新动作，其路径可能已经改变，要先根据ID删除之
            removeRouteById(route.getId());
        }

        // 添加到全量路由规则
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

    /**
     * 将路由规则添加到相应的组中。
     *
     * @param route 路由规则
     * @param normalizePath 标准化后的路径
     */
    private void addRouteToGroup(RouteRule route, String normalizePath) {
        RouteRuleGroup targetGroup;
        if (pathMatcher.isPattern(normalizePath)) {
            // 带正则表达式的路由规则
            String prefix = PathUtil.constantPrefix(normalizePath);
            targetGroup = patternPathRoutes.computeIfAbsent(prefix, k -> new RouteRuleGroup());
        } else {
            // 常量路径的路由规则
            targetGroup = constantPathRoutes.computeIfAbsent(normalizePath, k -> new RouteRuleGroup());
        }
        targetGroup.addRoute(route);
    }

    /**
     * 根据路由ID移除路由规则。
     *
     * @param routeId 路由ID
     * @return 被移除的路由规则
     */
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

    /**
     * 从模式路径组中移除路由规则。
     *
     * @param routeId 路由ID
     * @param normalizePath 标准化后的路径
     */
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

    /**
     * 从常量路径组中移除路由规则。
     *
     * @param routeId 路由ID
     * @param normalizePath 标准化后的路径
     */
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
     * 初始化路由规则。
     */
    @Override
    public void init() {
        // 从路由定义仓库加载路由定义并初始化
        if (routeDefinitionRepository != null) {
            routeDefinitionRepository.findAll().forEach(definition -> {
                RouteRule routeRule = convertToRouteRule(definition);
                addRoute(routeRule);
            });
        }
    }

    /**
     * 将路由定义转换为路由规则。
     *
     * @param definition 路由定义
     * @return 路由规则
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

        // 构建路由规则
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
     * 将过滤器定义转换为具体的过滤器实例。
     *
     * @param filterDef 过滤器定义
     * @return 过滤器实例
     */
    private RouteRuleFilter convertToFilter(FilterDefinition filterDef) {
        try {
            // 通过过滤器工厂创建具体的过滤器实例
            return filterFactoryMap.get(filterDef.getName()).create(filterDef.getArgs());
        } catch (Exception e) {
            log.error("Failed to create filter from definition: {}", filterDef, e);
            return null;
        }
    }

    /**
     * 将断言定义转换为断言实例。
     *
     * @param predicates 断言定义列表
     * @return 断言实例
     */
    private RoutePredicate convertToPredicate(List<PredicateDefinition> predicates) {
        List<RoutePredicate> routePredicates = new ArrayList<>();

        for (PredicateDefinition predicate : predicates) {
            try {
                RoutePredicate routePredicate = predicateFactoryMap.get(predicate.getName()).create(predicate.getArgs());
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

    /**
     * 路由规则组，用于存储和管理一组路由规则。
     */
    private static class RouteRuleGroup {
        private volatile boolean changed = false;
        private final Map<String, RouteRule> routes = new HashMap<>(4);
        private List<RouteRule> cachedRoutes = Collections.emptyList();

        /**
         * 获取路由规则列表。
         *
         * @return 路由规则列表
         */
        List<RouteRule> getRoutes() {
            if (changed) {
                cachedRoutes = new ArrayList<>(routes.values());
                changed = false;
            }
            return cachedRoutes;
        }

        /**
         * 添加路由规则。
         *
         * @param route 路由规则
         */
        void addRoute(RouteRule route) {
            routes.put(route.getId(), route);
            changed = true;
        }

        /**
         * 根据路由ID移除路由规则。
         *
         * @param id 路由ID
         */
        void removeRouteById(String id) {
            changed = Objects.nonNull(routes.remove(id));
        }

        /**
         * 检查路由规则组是否为空。
         *
         * @return 如果为空，返回 true；否则返回 false
         */
        boolean isEmpty() {
            return routes.isEmpty();
        }
    }
}
