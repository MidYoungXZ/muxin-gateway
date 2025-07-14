package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.filter.*;
import com.muxin.gateway.core.plus.route.predicate.*;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

/**
 * 路由配置转换器
 * 将YAML配置转换为Route对象
 * 内部维护各种Factory，简化设计
 *
 * @author muxin
 */
@Slf4j
public class RouteConfigConverter {

    // 在转换器内部维护FilterFactory映射
    private final Map<String, FilterFactory> filterFactories;

    // 在转换器内部维护PredicateFactory映射
    private final Map<String, PredicateFactory> predicateFactories;

    // 在转换器内部维护RouteTargetFactory映射
    private final RouteTargetFactory routeTargetFactory;

    public RouteConfigConverter() {
        this.filterFactories = initFilterFactories();
        this.predicateFactories = initPredicateFactories();
        this.routeTargetFactory = initRouteTargetFactories();
    }

    /**
     * 初始化FilterFactory映射
     */
    private Map<String, FilterFactory> initFilterFactories() {
        Map<String, FilterFactory> factories = new HashMap<>();

        // 注册内置FilterFactory
        registerFilterFactory(factories, new HttpLoggingFilterFactory());
        registerFilterFactory(factories, new HttpAuthFilterFactory());
        // TODO: 添加更多内置FilterFactory
        // registerFilterFactory(factories, new CorsFilterFactory());
        // registerFilterFactory(factories, new RateLimitFilterFactory());

        log.info("初始化FilterFactory完成，支持的Filter类型: {}", factories.keySet());
        return factories;
    }

    /**
     * 初始化PredicateFactory映射
     */
    private Map<String, PredicateFactory> initPredicateFactories() {
        Map<String, PredicateFactory> factories = new HashMap<>();

        // 注册内置PredicateFactory
        registerPredicateFactory(factories, new HttpPathPredicateFactory());
        registerPredicateFactory(factories, new HttpMethodPredicateFactory());
        registerPredicateFactory(factories, new HttpHeaderPredicateFactory());
        // TODO: 添加更多内置PredicateFactory
        // registerPredicateFactory(factories, new HttpQueryPredicateFactory());
        // registerPredicateFactory(factories, new HttpHostPredicateFactory());

        log.info("初始化PredicateFactory完成，支持的Predicate类型: {}", factories.keySet());
        return factories;
    }

    /**
     * 初始化RouteTargetFactory映射
     */
    private RouteTargetFactory initRouteTargetFactories() {
        //todo  注册内置RouteTargetFactory
        return null;
    }

    /**
     * 注册FilterFactory
     */
    private void registerFilterFactory(Map<String, FilterFactory> factories, FilterFactory factory) {
        String filterName = factory.getSupportedFilterName();
        factories.put(filterName, factory);
        log.debug("注册FilterFactory: {}", filterName);
    }

    /**
     * 注册PredicateFactory
     */
    private void registerPredicateFactory(Map<String, PredicateFactory> factories, PredicateFactory factory) {
        String predicateName = factory.getSupportedPredicateName();
        factories.put(predicateName, factory);
        log.debug("注册PredicateFactory: {}", predicateName);
    }

    /**
     * 注册自定义FilterFactory（支持运行时扩展）
     */
    public void registerFilterFactory(FilterFactory factory) {
        filterFactories.put(factory.getSupportedFilterName(), factory);
        log.info("注册自定义FilterFactory: {}", factory.getSupportedFilterName());
    }

    /**
     * 注册自定义PredicateFactory（支持运行时扩展）
     */
    public void registerPredicateFactory(PredicateFactory factory) {
        predicateFactories.put(factory.getSupportedPredicateName(), factory);
        log.info("注册自定义PredicateFactory: {}", factory.getSupportedPredicateName());
    }

    /**
     * 注册RouteTargetFactory
     */
    private void registerRouteTargetFactory(Map<ServiceType, RouteTargetFactory> factories, RouteTargetFactory factory) {
        ServiceType targetType = factory.getSupportedType();
        factories.put(targetType, factory);
        log.debug("注册RouteTargetFactory: {}", targetType);
    }


    /**
     * 将RouteDefinition转换为EnhancedRoute
     */
    public EnhancedRoute convertToRoute(RouteDefinition config) {
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

            return EnhancedRoute.builder()
                    .id(config.getId())
                    .name(config.getName())
                    .description(config.getDescription())
                    .order(config.getOrder())
                    .enabled(config.isEnabled())
                    .inboundProtocol(inboundProtocol)
                    .predicates(predicates)  // 每个路由独立的Predicate实例
                    .filters(filters)  // 每个路由独立的Filter实例
                    .target(target)
                    .timeouts(timeouts)
                    .metadata(config.getMetadata())
                    .build();

        } catch (Exception e) {
            log.error("转换路由配置失败: {}", config.getId(), e);
            throw new IllegalArgumentException("转换路由配置失败: " + config.getId(), e);
        }
    }

    /**
     * 批量转换路由配置
     */
    public List<EnhancedRoute> convertToRoutes(List<RouteDefinition> configs) {
        List<EnhancedRoute> routes = new ArrayList<>();

        for (RouteDefinition config : configs) {
            try {
                EnhancedRoute route = convertToRoute(config);
                routes.add(route);
                log.debug("成功转换路由: {}", config.getId());
            } catch (Exception e) {
                log.error("转换路由失败，跳过: {}", config.getId(), e);
                // 继续处理其他路由，不中断整个转换过程
            }
        }

        log.info("路由转换完成，成功: {}, 失败: {}", routes.size(), configs.size() - routes.size());
        return routes;
    }

    /**
     * 转换断言配置为Predicate实例
     * 每个路由的Predicate都是独立实例
     */
    private List<Predicate> convertPredicates(String routeId, List<PredicateDefinition> predicateConfigs) {
        if (predicateConfigs == null || predicateConfigs.isEmpty()) {
            return List.of();
        }

        List<Predicate> predicates = new ArrayList<>();

        for (PredicateDefinition config : predicateConfigs) {
            try {
                // 获取对应的Factory
                PredicateFactory factory = predicateFactories.get(config.getType());
                if (factory == null) {
                    log.error("不支持的断言类型: {} (路由: {})", config.getType(), routeId);
                    // 跳过不支持的断言，继续处理其他断言
                    continue;
                }

                // 验证配置
                factory.validateConfig(config);

                // 创建Predicate实例（每个路由独立）
                Predicate predicate = factory.createPredicate(config);
                predicates.add(predicate);

                log.debug("为路由 {} 创建断言: {}", routeId, config.getType());

            } catch (Exception e) {
                log.error("创建断言失败，跳过: {} (路由: {})", config.getType(), routeId, e);
                // 按照要求：跳过创建失败的Predicate，但打印异常，继续处理其他断言
            }
        }

        log.debug("路由 {} 断言链创建完成，包含 {} 个断言", routeId, predicates.size());
        return predicates;
    }

    /**
     * 转换过滤器配置为Filter实例
     * 每个路由的Filter都是独立实例
     */
    private List<Filter> convertFilters(String routeId, List<FilterDefinition> filterConfigs) {
        if (filterConfigs == null || filterConfigs.isEmpty()) {
            return List.of();
        }

        List<Filter> filters = new ArrayList<>();

        for (FilterDefinition config : filterConfigs) {
            if (!config.isEnabled()) {
                log.debug("跳过已禁用的过滤器: {} (路由: {})", config.getType(), routeId);
                continue;
            }

            try {
                // 获取对应的Factory
                FilterFactory factory = filterFactories.get(config.getType());
                if (factory == null) {
                    log.error("不支持的过滤器类型: {} (路由: {})", config.getType(), routeId);
                    // 跳过不支持的过滤器，继续处理其他过滤器
                    continue;
                }

                // 验证配置
                factory.validateConfig(config);

                // 创建Filter实例（每个路由独立）
                Filter filter = factory.createFilter(config);
                filters.add(filter);

                log.debug("为路由 {} 创建过滤器: {} (order: {})",
                        routeId, config.getType(), config.getOrder());

            } catch (Exception e) {
                log.error("创建过滤器失败，跳过: {} (路由: {})", config.getType(), routeId, e);
                // 按照要求：跳过创建失败的Filter，但打印异常，继续处理其他过滤器
            }
        }

        // 按order排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));

        log.debug("路由 {} 过滤器链创建完成，包含 {} 个过滤器", routeId, filters.size());
        return filters;
    }

    /**
     * 转换路由目标配置为RouteTarget实例
     */
    private RouteService convertRouteTarget(ServiceDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("路由目标配置不能为空");
        }

        ServiceType type = definition.getType();
        if (type == null) {
            throw new IllegalArgumentException("路由目标类型不能为空");
        }
        return routeTargetFactory.createRouteTarget(definition);
    }

    /**
     * 轮询选择
     */
    private EndpointAddress roundRobinSelect(List<EndpointAddress> targets, RequestContext context) {
        // 简单的轮询实现
        long requestId = System.nanoTime();
        int index = (int) (requestId % targets.size());
        return targets.get(index);
    }

    /**
     * 随机选择
     */
    private EndpointAddress randomSelect(List<EndpointAddress> targets) {
        int index = new Random().nextInt(targets.size());
        return targets.get(index);
    }

    /**
     * 加权轮询选择
     */
    private EndpointAddress weightedRoundRobinSelect(
            List<EndpointAddress> targets,
            RequestContext context) {
        // 简单实现：目前返回第一个，实际应该根据权重选择
        // TODO: 实现真正的加权轮询算法
        return targets.get(0);
    }

    /**
     * 一致性哈希选择
     */
    private EndpointAddress consistentHashSelect(
            List<EndpointAddress> targets,
            RequestContext context,
            RouteService routeService) {
        // 简单实现：根据请求的某个属性进行哈希
        String hashKey = context != null ? context.toString() : String.valueOf(System.nanoTime());
        int hash = hashKey.hashCode();
        int index = Math.abs(hash) % targets.size();
        return targets.get(index);
    }

    /**
     * 转换超时配置
     */
    private TimeoutConfig convertTimeouts(TimeoutConfig config) {
        if (config == null) {
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
            log.warn("无法解析时间格式: {}, 使用默认值", durationStr);
            return null;
        }
    }
} 