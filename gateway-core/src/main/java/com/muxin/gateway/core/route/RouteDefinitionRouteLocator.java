package com.muxin.gateway.core.route;

import com.muxin.gateway.core.cache.RouteCache;
import com.muxin.gateway.core.route.filter.factory.FilterFactory;
import com.muxin.gateway.core.route.predicate.factory.PredicateFactory;
import com.muxin.gateway.core.route.filter.PartFilter;
import com.muxin.gateway.core.route.filter.FilterDefinition;
import com.muxin.gateway.core.route.path.AntPathMatcher;
import com.muxin.gateway.core.route.path.PathMatcher;
import com.muxin.gateway.core.route.path.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.muxin.gateway.core.common.GatewayConstants.*;

/**
 * 路由定位器实现，同时作为RouteDefinition和RouteLocator的桥梁
 */
@Component
@Slf4j
public class RouteDefinitionRouteLocator implements RouteLocator {
    /**
     * 路由定义定位器
     */
    private final RouteDefinitionRepository routeDefinitionRepository;

    /**
     * 路由缓存
     */
    private final RouteCache routeCache;

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
     * 过滤器工厂Map集合
     */
    private final Map<String,FilterFactory> filterFactoryMap;
    /**
     * 断言工厂Map集合
     */
    private final Map<String,PredicateFactory> predicateFactoryMap;

    public RouteDefinitionRouteLocator(RouteDefinitionRepository routeDefinitionRepository, Map<String, FilterFactory> filterFactoryMap, Map<String, PredicateFactory> predicateFactoryMap, RouteCache routeCache) {
        this.routeDefinitionRepository = routeDefinitionRepository;
        this.filterFactoryMap = filterFactoryMap;
        this.predicateFactoryMap = predicateFactoryMap;
        this.routeCache = routeCache;
        init();
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
        
        // 先从缓存中查找
        RouteRule cachedRule = routeCache.get(path);
        if (cachedRule != null) {
            return Collections.singletonList(cachedRule);
        }
        
        //先解决常量路径
        String normalizePath = PathUtil.normalize(path);
        log.debug("Looking for routes for path: {} (normalized: {})", path, normalizePath);
        log.debug("Available constant paths: {}", constantPathRoutes.keySet());
        log.debug("Available pattern paths: {}", patternPathRoutes.keySet());
        
        RouteRuleGroup routeRuleGroup = constantPathRoutes.get(normalizePath);
        List<RouteRuleGroup> groups = findInPatternPathRoutes(normalizePath);
        
        if (Objects.nonNull(routeRuleGroup)) {
            log.debug("Found constant path match for: {}", normalizePath);
            groups.add(0, routeRuleGroup);
        }
        
        if (groups.isEmpty()) {
            log.debug("No routes found for path: {}", path);
            return Collections.emptyList();
        } else if (groups.size() == 1) {
            List<RouteRule> routes = groups.get(0).getRoutes();
            log.debug("Found {} routes for path: {}", routes.size(), path);
            // 缓存第一个匹配的规则
            if (!routes.isEmpty()) {
                routeCache.put(path, routes.get(0));
            }
            return routes;
        }

        // 合并多个组的路由规则
        List<RouteRule> mergedRoutes = new ArrayList<>(groups.size() * 2);
        for (RouteRuleGroup rg : groups) {
            mergedRoutes.addAll(rg.getRoutes());
        }
        log.debug("Found {} merged routes for path: {}", mergedRoutes.size(), path);
        
        // 缓存第一个匹配的规则
        if (!mergedRoutes.isEmpty()) {
            routeCache.put(path, mergedRoutes.get(0));
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
        
        // 从路由的metadata中获取path配置，如果没有则使用URI的path
        String routePath = extractPathFromRoute(route);
        if (routePath == null || routePath.isEmpty()) {
            log.warn("No valid path found for route: {}", route.getId());
            return;
        }
        
        String normalizePath = PathUtil.normalize(routePath);
        log.debug("Adding route {} with normalized path: {}", route.getId(), normalizePath);
        
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
        String routePath = extractPathFromRoute(removedRoute);
        if (routePath != null && !routePath.isEmpty()) {
            String normalizePath = PathUtil.normalize(routePath);

            if (pathMatcher.isPattern(normalizePath)) {
                removeFromPatternGroup(routeId, normalizePath);
            } else {
                removeFromConstantGroup(routeId, normalizePath);
            }
        }

        return removedRoute;
    }
    
    /**
     * 从RouteRule中提取路径，优先从metadata中的pathPattern获取，其次使用URI的path
     */
    private String extractPathFromRoute(RouteRule route) {
        // 先尝试从metadata中获取path pattern
        if (route.getMetadata() != null) {
            Object pathPattern = route.getMetadata().get("pathPattern");
            if (pathPattern instanceof String && !((String) pathPattern).isEmpty()) {
                return (String) pathPattern;
            }
        }
        
        // 如果metadata中没有，则使用URI的path
        if (route.getUri() != null && route.getUri().getPath() != null && !route.getUri().getPath().isEmpty()) {
            return route.getUri().getPath();
        }
        
        return null;
    }
    
    /**
     * 从断言定义列表中提取Path断言的pattern
     */
    private String extractPathPattern(List<PredicateDefinition> predicates) {
        if (predicates == null) {
            return null;
        }
        
        for (PredicateDefinition predicate : predicates) {
            if (PATH_PREDICATE_NAME.equals(predicate.getName())) {
                Map<String, String> args = predicate.getArgs();
                if (args != null) {
                    // 尝试不同的参数名
                    String pattern = args.get(PATTERN_ARG);
                    if (pattern == null) {
                        pattern = args.get(GENKEY_PREFIX + "0");
                    }
                    if (pattern != null && !pattern.isEmpty()) {
                        return pattern;
                    }
                }
            }
        }
        
        return null;
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
        loadRoutes();
    }

    /**
     * 加载路由配置
     */
    private void loadRoutes() {
        // 清空现有路由
        allRoutes.clear();
        constantPathRoutes.clear();
        patternPathRoutes.clear();
        
        // 清空路由缓存
        routeCache.invalidateAll();

        if (routeDefinitionRepository != null) {
            Iterable<RouteDefinition> definitions = routeDefinitionRepository.findAll();
            if (definitions != null) {
                definitions.forEach(definition -> {
                    if (definition != null) {
                        RouteRule routeRule = convertToRouteRule(definition);
                        addRoute(routeRule);
                    }
                });
            }
        }
    }

    /**
     * 将RouteDefinition转换为RouteRule
     */
    private RouteRule convertToRouteRule(RouteDefinition definition) {
        // 转换过滤器定义
        List<PartFilter> filters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(definition.getFilters())) {
            for (FilterDefinition filterDef : definition.getFilters()) {
                PartFilter filter = convertToFilter(filterDef);
                if (filter != null) {
                    filters.add(filter);
                }
            }
        }

        // 转换断言定义
        RoutePredicate predicate = null;
        String pathPattern = null;
        if (!CollectionUtils.isEmpty(definition.getPredicates())) {
            predicate = convertToPredicate(definition.getPredicates());
            // 提取Path断言的pattern
            pathPattern = extractPathPattern(definition.getPredicates());
        }

        // 复制metadata并添加pathPattern
        Map<String, Object> metadata = new HashMap<>();
        if (definition.getMetadata() != null) {
            metadata.putAll(definition.getMetadata());
        }
        if (pathPattern != null) {
            metadata.put("pathPattern", pathPattern);
        }

        // 构建RouteRule
        return RouteRule.builder()
                .id(definition.getId())
                .uri(definition.getUri())
                .order(definition.getOrder())
                .routeRuleFilters(filters)
                .predicate(predicate != null ? predicate : exchange -> true)
                .metadata(metadata)
                .build();
    }

    /**
     * 转换过滤器定义为具体的过滤器实例
     */
    private PartFilter convertToFilter(FilterDefinition filterDef) {
        try {
            FilterFactory factory = filterFactoryMap.get(filterDef.getName());
            if (factory == null) {
                log.error("No FilterFactory found for filter name: {}. Available factories: {}", 
                    filterDef.getName(), filterFactoryMap.keySet());
                return null;
            }
            // 这里需要通过FilterFactory来创建具体的过滤器实例
            return factory.create(filterDef.getArgs());
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
                PredicateFactory factory = predicateFactoryMap.get(predicate.getName());
                if (factory == null) {
                                    log.error("No PredicateFactory found for predicate name: {}. Available factories: {}", 
                    predicate.getName(), predicateFactoryMap.keySet());
                    continue;
                }
                RoutePredicate routePredicate = factory.create(predicate.getArgs());
                if (routePredicate != null) {
                    routePredicates.add(routePredicate);
                }
            } catch (Exception e) {
                log.error("Failed to create predicate from definition: {}", predicate, e);
            }
        }

        // 组合多个断言（AND关系）
        return exchange -> routePredicates.stream().allMatch(p -> p.test(exchange));
    }

    @Override
    public List<RouteRule> getAllRoutes() {
        return new ArrayList<>(allRoutes.values());
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
