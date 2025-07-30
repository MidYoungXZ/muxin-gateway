package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.filter.*;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategyFactory;
import com.muxin.gateway.core.plus.route.predicate.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由配置转换器
 * 将YAML配置转换为Route对象
 * 内部维护各种Factory，简化设计
 *
 * @author muxin
 */
@Slf4j
public class RouteConfigConverter {

    // ========== Factory注册表 ==========
    /**
     * 过滤器工厂映射
     */
    private final Map<String, FilterFactory> filterFactories;

    /**
     * 断言工厂映射
     */
    private final Map<String, PredicateFactory> predicateFactories;

    /**
     * 路由服务工厂映射
     */
    private final Map<ServiceType, RouteServiceFactory> routeServiceFactories;



    // ========== 状态管理 ==========
    private volatile boolean initialized = false;

    public RouteConfigConverter() {
        this.filterFactories = new ConcurrentHashMap<>();
        this.predicateFactories = new ConcurrentHashMap<>();
        this.routeServiceFactories = new ConcurrentHashMap<>();
        
        // 初始化工厂
        initializeFactories();
        
        log.info("[RouteConfigConverter] 路由配置转换器创建完成");
    }

    // ========== 初始化方法 ==========

    /**
     * 初始化所有工厂
     */
    private void initializeFactories() {
        try {
            initFilterFactories();
            initPredicateFactories();
            initRouteServiceFactories();
            
            initialized = true;
            log.info("[RouteConfigConverter] 所有工厂初始化完成 - Filter: {}, Predicate: {}, RouteService: {}", 
                    filterFactories.size(), predicateFactories.size(), routeServiceFactories.size());
                    
        } catch (Exception e) {
            log.error("[RouteConfigConverter] 工厂初始化失败", e);
            throw new RuntimeException("工厂初始化失败", e);
        }
    }

    /**
     * 初始化FilterFactory映射
     */
    private void initFilterFactories() {
        // TODO: 后续可以注册更多内置FilterFactory
        log.info("[RouteConfigConverter] FilterFactory初始化完成，支持的Filter类型: {}", filterFactories.keySet());
    }

    /**
     * 初始化PredicateFactory映射
     */
    private void initPredicateFactories() {
        // TODO: 后续可以注册更多内置PredicateFactory
        log.info("[RouteConfigConverter] PredicateFactory初始化完成，支持的Predicate类型: {}", predicateFactories.keySet());
    }

    /**
     * 初始化RouteServiceFactory映射
     */
    private void initRouteServiceFactories() {
        // 注册内置RouteServiceFactory
        registerRouteServiceFactory(new ConfigRouteServiceFactory());
        // TODO: 后续可以注册服务发现工厂（需要ServiceRegistry依赖）
        // registerRouteServiceFactory(new DiscoveryRouteServiceFactory(serviceRegistry));

        log.info("[RouteConfigConverter] RouteServiceFactory初始化完成，支持的服务类型: {}", routeServiceFactories.keySet());
    }

    // ========== 工厂注册方法 ==========

    /**
     * 注册FilterFactory
     */
    private void registerFilterFactory(FilterFactory factory) {
        String filterName = factory.getSupportedFilterName();
        filterFactories.put(filterName, factory);
        log.debug("[RouteConfigConverter] 注册FilterFactory: {}", filterName);
    }

    /**
     * 注册PredicateFactory
     */
    private void registerPredicateFactory(PredicateFactory factory) {
        String predicateName = factory.getSupportedPredicateName();
        predicateFactories.put(predicateName, factory);
        log.debug("[RouteConfigConverter] 注册PredicateFactory: {}", predicateName);
    }

    /**
     * 注册RouteServiceFactory
     */
    private void registerRouteServiceFactory(RouteServiceFactory factory) {
        ServiceType serviceType = factory.getSupportedType();
        routeServiceFactories.put(serviceType, factory);
        log.debug("[RouteConfigConverter] 注册RouteServiceFactory: {}", serviceType);
    }

    // ========== 运行时扩展方法 ==========

    /**
     * 注册自定义FilterFactory（支持运行时扩展）
     */
    public void registerCustomFilterFactory(FilterFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("FilterFactory不能为空");
        }
        
        String filterName = factory.getSupportedFilterName();
        filterFactories.put(filterName, factory);
        
        log.info("[RouteConfigConverter] 注册自定义FilterFactory: {}", filterName);
    }

    /**
     * 注册自定义PredicateFactory（支持运行时扩展）
     */
    public void registerCustomPredicateFactory(PredicateFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("PredicateFactory不能为空");
        }
        
        String predicateName = factory.getSupportedPredicateName();
        predicateFactories.put(predicateName, factory);
        
        log.info("[RouteConfigConverter] 注册自定义PredicateFactory: {}", predicateName);
    }

    /**
     * 注册自定义RouteServiceFactory（支持运行时扩展）
     */
    public void registerCustomRouteServiceFactory(RouteServiceFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("RouteServiceFactory不能为空");
        }
        
        ServiceType serviceType = factory.getSupportedType();
        routeServiceFactories.put(serviceType, factory);
        
        log.info("[RouteConfigConverter] 注册自定义RouteServiceFactory: {}", serviceType);
    }

    // ========== 核心转换方法 ==========

    /**
     * 将RouteDefinition转换为EnhancedRoute
     */
    public Route convertToRoute(RouteDefinition config) {
        if (config == null) {
            throw new IllegalArgumentException("路由配置不能为空");
        }
        
        try {
            // 验证配置
            config.validate();

            // 转换协议
            Protocol inboundProtocol = config.getSupportProtocol().toProtocol();

            // 转换断言（传入routeId，确保每个路由的Predicate独立）
            List<Predicate> predicates = convertPredicates(config.getId(), config.getPredicates());

            // 转换过滤器（传入routeId，确保每个路由的Filter独立）
            List<Filter> filters = convertFilters(config.getId(), config.getFilters());

            // 转换路由目标
            RouteService target = convertRouteTarget(config.getService());

            // 转换超时配置
            TimeoutConfig timeouts = convertTimeouts(config.getTimeouts());
            
            // 创建负载均衡策略（支持null配置）
            LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategyFactory.createStrategy(
                config.getLoadBalance()  // 可能为null，工厂内部处理
            );

            // 创建Route实例
            DefaultRoute route = DefaultRoute.builder()
                    .id(config.getId())
                    .name(config.getName())
                    .description(config.getDescription())
                    .order(config.getOrder())
                    .enabled(config.isEnabled())
                    .supportedProtocol(inboundProtocol)
                    .predicates(predicates)
                    .filters(filters)
                    .service(target)
                    .loadBalanceStrategy(loadBalanceStrategy)
                    .metadata(config.getMetadata())
                    .timeoutConfig(timeouts)
                    .build();

            log.debug("[RouteConfigConverter] 成功转换路由: {} (策略: {})", 
                    config.getId(), loadBalanceStrategy.getStrategyName());
            
            return route;

        } catch (Exception e) {
            log.error("[RouteConfigConverter] 转换路由配置失败: {}", config.getId(), e);
            throw new IllegalArgumentException("转换路由配置失败: " + config.getId(), e);
        }
    }

    /**
     * 批量转换路由配置
     */
    public List<Route> convertToRoutes(List<RouteDefinition> configs) {
        if (configs == null || configs.isEmpty()) {
            log.warn("[RouteConfigConverter] 路由配置列表为空");
            return new ArrayList<>();
        }

        List<Route> routes = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (RouteDefinition config : configs) {
            try {
                Route route = convertToRoute(config);
                routes.add(route);
                log.debug("[RouteConfigConverter] 成功转换路由: {}", config.getId());
            } catch (Exception e) {
                log.error("[RouteConfigConverter] 转换路由失败，跳过: {}", config.getId(), e);
                // 继续处理其他路由，不中断整个转换过程
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("[RouteConfigConverter] 批量路由转换完成，成功: {}, 失败: {}, 总耗时: {}ms", 
                routes.size(), configs.size() - routes.size(), totalTime);
        
        return routes;
    }

    // ========== 子组件转换方法 ==========

    /**
     * 转换断言配置为Predicate实例
     * 每个路由的Predicate都是独立实例
     */
    private List<Predicate> convertPredicates(String routeId, List<PredicateDefinition> predicateConfigs) {
        if (predicateConfigs == null || predicateConfigs.isEmpty()) {
            log.debug("[RouteConfigConverter] 路由 {} 没有配置断言", routeId);
            return new ArrayList<>();
        }

        List<Predicate> predicates = new ArrayList<>();

        for (PredicateDefinition config : predicateConfigs) {
            try {
                // 获取对应的Factory
                PredicateFactory factory = predicateFactories.get(config.getType());
                if (factory == null) {
                    log.error("[RouteConfigConverter] 不支持的断言类型: {} (路由: {})", config.getType(), routeId);
                    continue;
                }

                // 验证配置
                factory.validateConfig(config);

                // 创建Predicate实例（每个路由独立）
                Predicate predicate = factory.createPredicate(config);
                predicates.add(predicate);

                log.debug("[RouteConfigConverter] 为路由 {} 创建断言: {}", routeId, config.getType());

            } catch (Exception e) {
                log.error("[RouteConfigConverter] 创建断言失败，跳过: {} (路由: {})", config.getType(), routeId, e);
            }
        }

        log.debug("[RouteConfigConverter] 路由 {} 断言链创建完成，包含 {} 个断言", routeId, predicates.size());
        return predicates;
    }

    /**
     * 转换过滤器配置为Filter实例
     * 每个路由的Filter都是独立实例
     */
    private List<Filter> convertFilters(String routeId, List<FilterDefinition> filterConfigs) {
        if (filterConfigs == null || filterConfigs.isEmpty()) {
            log.debug("[RouteConfigConverter] 路由 {} 没有配置过滤器", routeId);
            return new ArrayList<>();
        }

        List<Filter> filters = new ArrayList<>();

        for (FilterDefinition config : filterConfigs) {
            if (!config.isEnabled()) {
                log.debug("[RouteConfigConverter] 跳过已禁用的过滤器: {} (路由: {})", config.getType(), routeId);
                continue;
            }

            try {
                // 获取对应的Factory
                FilterFactory factory = filterFactories.get(config.getType());
                if (factory == null) {
                    log.error("[RouteConfigConverter] 不支持的过滤器类型: {} (路由: {})", config.getType(), routeId);
                    continue;
                }

                // 验证配置
                factory.validateConfig(config);

                // 创建Filter实例（每个路由独立）
                Filter filter = factory.createFilter(config);
                filters.add(filter);

                log.debug("[RouteConfigConverter] 为路由 {} 创建过滤器: {} (order: {})",
                        routeId, config.getType(), config.getOrder());

            } catch (Exception e) {
                log.error("[RouteConfigConverter] 创建过滤器失败，跳过: {} (路由: {})", config.getType(), routeId, e);
            }
        }

        // 按order排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));

        log.debug("[RouteConfigConverter] 路由 {} 过滤器链创建完成，包含 {} 个过滤器", routeId, filters.size());
        return filters;
    }

    /**
     * 转换路由目标配置为RouteService实例
     */
    private RouteService convertRouteTarget(ServiceDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("路由目标配置不能为空");
        }

        ServiceType type = definition.getType();
        if (type == null) {
            throw new IllegalArgumentException("路由目标类型不能为空");
        }

        // 获取对应的工厂
        RouteServiceFactory factory = routeServiceFactories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("不支持的服务类型: " + type);
        }

        try {
            return factory.createRouteTarget(definition);
        } catch (Exception e) {
            log.error("[RouteConfigConverter] 创建路由目标失败: {}", definition.getId(), e);
            throw new RuntimeException("创建路由目标失败: " + e.getMessage(), e);
        }
    }



    /**
     * 转换超时配置
     */
    private TimeoutConfig convertTimeouts(TimeoutConfig config) {
        if (config == null) {
            log.debug("[RouteConfigConverter] 使用默认超时配置");
            return TimeoutConfig.defaultConfig();
        }
        return config;
    }

    /**
     * 解析时间字符串为Duration
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 支持简单的时间格式解析，如: "30s", "5m", "1h"
            String trimmed = durationStr.trim().toLowerCase();

            if (trimmed.endsWith("s")) {
                long seconds = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofSeconds(seconds);
            } else if (trimmed.endsWith("m")) {
                long minutes = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofMinutes(minutes);
            } else if (trimmed.endsWith("h")) {
                long hours = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
                return Duration.ofHours(hours);
            } else if (trimmed.endsWith("ms")) {
                long millis = Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
                return Duration.ofMillis(millis);
            } else {
                // 默认按秒解析
                long seconds = Long.parseLong(trimmed);
                return Duration.ofSeconds(seconds);
            }
        } catch (NumberFormatException e) {
            log.warn("[RouteConfigConverter] 无法解析时间格式: {}, 使用默认值", durationStr);
            return null;
        }
    }



    // ========== 状态查询方法 ==========

    public boolean isInitialized() {
        return initialized;
    }

    public Set<String> getSupportedFilterTypes() {
        return new HashSet<>(filterFactories.keySet());
    }

    public Set<String> getSupportedPredicateTypes() {
        return new HashSet<>(predicateFactories.keySet());
    }

    public Set<ServiceType> getSupportedServiceTypes() {
        return new HashSet<>(routeServiceFactories.keySet());
    }



    @Override
    public String toString() {
        return String.format("RouteConfigConverter{initialized=%s, filters=%d, predicates=%d, services=%d}", 
                initialized, filterFactories.size(), predicateFactories.size(), routeServiceFactories.size());
    }
} 