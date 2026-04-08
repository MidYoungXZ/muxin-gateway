package com.muxin.gateway.config.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.muxin.gateway.admin.entity.GwPlugin;
import com.muxin.gateway.admin.entity.GwPredicate;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.entity.GwRoutePlugin;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import com.muxin.gateway.admin.mapper.PluginMapper;
import com.muxin.gateway.admin.mapper.PredicateMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.mapper.RoutePluginMapper;
import com.muxin.gateway.admin.constants.PluginConfigKeys;
import com.muxin.gateway.constants.FilterConfigKeys;
import static com.muxin.gateway.admin.entity.table.GwRoutePluginTableDef.GW_ROUTE_PLUGIN;
import static com.muxin.gateway.admin.entity.table.GwPluginTableDef.GW_PLUGIN;
import static com.muxin.gateway.admin.entity.table.GwRoutePredicateTableDef.GW_ROUTE_PREDICATE;
import static com.muxin.gateway.admin.entity.table.GwPredicateTableDef.GW_PREDICATE;
import com.muxin.gateway.core.config.provider.ConfigChangedEvent;
import com.muxin.gateway.core.config.provider.ConfigChangeListener;
import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.route.RouteDefinition;
import com.muxin.gateway.core.route.filter.FilterDefinition;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.route.predicate.PredicateDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseRouteConfigProvider implements RouteConfigProvider {

    private static final String SOURCE = "DATABASE";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RouteMapper routeMapper;
    private final RoutePredicateMapper routePredicateMapper;
    private final RoutePluginMapper routePluginMapper;
    private final PluginMapper pluginMapper;
    private final PredicateMapper predicateMapper;
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private volatile List<RouteDefinition> cachedRoutes = new ArrayList<>();
    private volatile boolean refreshing = false;

    @Override
    public List<RouteDefinition> getRoutes() {
        if (cachedRoutes.isEmpty() && !refreshing) {
            refresh();
        }
        return Collections.unmodifiableList(cachedRoutes);
    }

    @Override
    public Optional<RouteDefinition> getRoute(String routeId) {
        return cachedRoutes.stream()
                .filter(r -> r.getId().equals(routeId))
                .findFirst();
    }

    @Override
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            if (log.isInfoEnabled()) {
                log.info("Refreshing route configuration from database");
            }

            List<GwRoute> routes = routeMapper.selectAll()
                    .stream()
                    .sorted(Comparator.comparingInt(r -> r.getOrder() != null ? r.getOrder() : 0))
                    .collect(Collectors.toList());

            List<RouteDefinition> newRoutes = new ArrayList<>();
            for (GwRoute route : routes) {
                try {
                    RouteDefinition definition = convertToRouteDefinition(route);
                    newRoutes.add(definition);
                } catch (Exception e) {
                    log.error("Failed to convert route: {}", route.getRouteId(), e);
                }
            }

            cachedRoutes = newRoutes;

            ConfigChangedEvent event = new ConfigChangedEvent(
                    ConfigChangedEvent.ChangeType.ROUTE_REFRESH_ALL,
                    newRoutes.stream().map(RouteDefinition::getId).toList(),
                    SOURCE
            );
            notifyListeners(event);

            if (log.isInfoEnabled()) {
                log.info("Loaded {} routes from database", cachedRoutes.size());
            }
        } finally {
            refreshing = false;
        }
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    private RouteDefinition convertToRouteDefinition(GwRoute route) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(route.getRouteId());
        definition.setName(route.getRouteName());
        definition.setDescription(route.getDescription());
        definition.setOrder(route.getOrder() != null ? route.getOrder() : 0);
        definition.setEnabled(route.getEnabled() != null ? route.getEnabled() : true);
        definition.setMetadata(route.getMetadata());

        String uri = route.getUri();
        if (uri != null && uri.startsWith("lb://")) {
            String serviceName = uri.substring(5);
            definition.setServiceRef(serviceName);
        } else if (uri != null) {
            definition.setServiceRef(extractServiceIdFromUri(uri));
        }

        if (route.getLoadBalanceStrategy() != null && !route.getLoadBalanceStrategy().isEmpty()) {
            LoadBalanceDefinition lb = new LoadBalanceDefinition();
            lb.setStrategy(route.getLoadBalanceStrategy());
            definition.setLoadBalance(lb);
        }

        List<PredicateDefinition> predicates = loadPredicates(route.getId());
        definition.setPredicates(predicates);

        List<FilterDefinition> filters = loadFiltersFromPlugins(route.getId());
        definition.setFilters(filters);

        return definition;
    }

    private String extractServiceIdFromUri(String uri) {
        try {
            java.net.URI parsedUri = new java.net.URI(uri);
            String host = parsedUri.getHost();
            return host != null ? host.replace(".", "-") : uri;
        } catch (Exception e) {
            return uri.replaceAll("[^a-zA-Z0-9_-]", "-");
        }
    }

    private List<PredicateDefinition> loadPredicates(Long routeId) {
        List<GwRoutePredicate> routePredicates = routePredicateMapper.selectListByQuery(
            QueryWrapper.create().where(GW_ROUTE_PREDICATE.ROUTE_ID.eq(routeId))
        );
        
        if (routePredicates.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> predicateIds = routePredicates.stream()
                .map(GwRoutePredicate::getPredicateId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, GwPredicate> predicateMap = predicateMapper.selectListByQuery(
                QueryWrapper.create()
                    .where(GW_PREDICATE.ID.in(predicateIds))
            ).stream()
            .collect(Collectors.toMap(GwPredicate::getId, p -> p));
        
        List<PredicateDefinition> predicates = new ArrayList<>();

        for (GwRoutePredicate rp : routePredicates) {
            GwPredicate predicate = predicateMap.get(rp.getPredicateId());
            if (predicate == null) continue;

            PredicateDefinition pd = PredicateDefinition.builder()
                    .name(predicate.getPredicateType())
                    .args(predicate.getArgs() != null ? predicate.getArgs() : new HashMap<>())
                    .build();
            predicates.add(pd);
        }

        return predicates;
    }

    private List<FilterDefinition> loadFiltersFromPlugins(Long routeId) {
        List<GwRoutePlugin> routePlugins = routePluginMapper.selectListByQuery(
            QueryWrapper.create().where(GW_ROUTE_PLUGIN.ROUTE_ID.eq(routeId))
        );
        
        if (routePlugins.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> pluginIds = routePlugins.stream()
                .map(GwRoutePlugin::getPluginId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, GwPlugin> pluginMap = pluginMapper.selectListByQuery(
                QueryWrapper.create()
                    .where(GW_PLUGIN.ID.in(pluginIds))
            ).stream()
            .collect(Collectors.toMap(GwPlugin::getId, p -> p));
        
        List<FilterDefinition> filters = new ArrayList<>();
        
        for (GwRoutePlugin rp : routePlugins) {
            GwPlugin plugin = pluginMap.get(rp.getPluginId());
            if (plugin == null) continue;
            
            String pluginName = plugin.getPluginName();
            String pluginType = plugin.getPluginType();
            Map<String, Object> routeConfig = rp.getConfig();
            Map<String, Object> defaultConfig = plugin.getDefaultConfig();
            Integer priorityOverride = rp.getPriorityOverride();
            Integer defaultPriority = plugin.getDefaultPriority();
            Boolean pluginEnabled = rp.getEnabled() != null ? rp.getEnabled() : true;

            if (!pluginEnabled) {
                continue;
            }

            Map<String, Object> effectiveConfig = mergeConfig(defaultConfig, routeConfig);
            int order = priorityOverride != null ? priorityOverride : 
                    (defaultPriority != null ? defaultPriority : 0);

            List<FilterDefinition> mapped = mapPluginToFilters(pluginName, pluginType, effectiveConfig, order);
            filters.addAll(mapped);
        }

        return filters;
    }

    private Map<String, Object> mergeConfig(Map<String, Object> defaults, Map<String, Object> overrides) {
        Map<String, Object> merged = new HashMap<>();
        if (defaults != null) {
            merged.putAll(defaults);
        }
        if (overrides != null) {
            merged.putAll(overrides);
        }
        return merged;
    }

private List<FilterDefinition> mapPluginToFilters(String pluginName, String pluginType,
                                                        Map<String, Object> config, int order) {
        if (!"FILTER".equals(pluginType)) {
            log.debug("Skipping non-FILTER plugin: {} (type={})", pluginName, pluginType);
            return List.of();
        }

        return switch (pluginName) {
            case "rate-limit" -> List.of(createRateLimitFilter(config, order));
            case "circuit-breaker" -> List.of(createCircuitBreakerFilter(config, order));
            case "cors" -> List.of(createCorsFilter(config, order));
            case "timeout" -> List.of(createTimeoutFilter(config, order));
            case "request-rewrite" -> List.of(createRequestRewriteFilter(config, order));
            case "response-rewrite" -> List.of(createResponseRewriteFilter(config, order));
            default -> {
                log.warn("No filter mapping for plugin: {}, skipping", pluginName);
                yield List.of();
            }
        };
    }

    private FilterDefinition createRateLimitFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();
        Object rate = config.get(PluginConfigKeys.RATE);
        if (rate != null) {
            filterArgs.put(FilterConfigKeys.REPLENISH_RATE, toInt(rate, 10));
        }
        Object burst = config.get(PluginConfigKeys.BURST);
        if (burst != null) {
            filterArgs.put(FilterConfigKeys.BURST_CAPACITY, toInt(burst, 20));
        }

        return FilterDefinition.builder()
                .name("RequestRateLimiter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    private FilterDefinition createCircuitBreakerFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();
        Object failureThreshold = config.get(PluginConfigKeys.FAILURE_THRESHOLD);
        if (failureThreshold != null) {
            filterArgs.put(FilterConfigKeys.FAILURE_RATE_THRESHOLD, toInt(failureThreshold, 50));
        }
        Object timeout = config.get(PluginConfigKeys.TIMEOUT);
        if (timeout != null) {
            filterArgs.put(FilterConfigKeys.WAIT_DURATION_IN_OPEN_STATE, toLong(timeout, 60000));
        }
        Object successThreshold = config.get(PluginConfigKeys.SUCCESS_THRESHOLD);
        if (successThreshold != null) {
            filterArgs.put(FilterConfigKeys.RING_BUFFER_SIZE, toInt(successThreshold, 100));
        }

        return FilterDefinition.builder()
                .name("CircuitBreaker")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    private FilterDefinition createCorsFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();
        filterArgs.put(FilterConfigKeys.ALLOW_ORIGINS, config.getOrDefault(PluginConfigKeys.ALLOW_ORIGINS, "*"));
        filterArgs.put(FilterConfigKeys.ALLOW_METHODS, config.getOrDefault(PluginConfigKeys.ALLOW_METHODS, "*"));
        filterArgs.put(FilterConfigKeys.ALLOW_HEADERS, config.getOrDefault(PluginConfigKeys.ALLOW_HEADERS, "*"));
        filterArgs.put(FilterConfigKeys.ALLOW_CREDENTIALS, config.getOrDefault(PluginConfigKeys.ALLOW_CREDENTIALS, false));
        filterArgs.put(FilterConfigKeys.MAX_AGE, config.getOrDefault(PluginConfigKeys.MAX_AGE, 3600));

        return FilterDefinition.builder()
                .name("CorsFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    private FilterDefinition createTimeoutFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();
        filterArgs.put(FilterConfigKeys.CONNECT_TIMEOUT, config.getOrDefault(PluginConfigKeys.CONNECT_TIMEOUT, 5000));
        filterArgs.put(FilterConfigKeys.RESPONSE_TIMEOUT, config.getOrDefault(PluginConfigKeys.RESPONSE_TIMEOUT, 30000));

        return FilterDefinition.builder()
                .name("TimeoutFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private FilterDefinition createRequestRewriteFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();

        if (config.get(PluginConfigKeys.PATH_REGEX) != null) {
            filterArgs.put(FilterConfigKeys.PATH_REGEX, config.get(PluginConfigKeys.PATH_REGEX));
        }
        if (config.get(PluginConfigKeys.PATH_REPLACEMENT) != null) {
            filterArgs.put(FilterConfigKeys.PATH_REPLACEMENT, config.get(PluginConfigKeys.PATH_REPLACEMENT));
        }
        if (config.get(PluginConfigKeys.HEADERS_TO_ADD) != null) {
            filterArgs.put(FilterConfigKeys.HEADERS_TO_ADD, convertHeadersToAdd(config.get(PluginConfigKeys.HEADERS_TO_ADD)));
        }
        if (config.get(PluginConfigKeys.HEADERS_TO_REMOVE) != null) {
            filterArgs.put(FilterConfigKeys.HEADERS_TO_REMOVE, config.get(PluginConfigKeys.HEADERS_TO_REMOVE));
        }

        return FilterDefinition.builder()
                .name("RequestRewriteFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private FilterDefinition createResponseRewriteFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();

        if (config.get(PluginConfigKeys.HEADERS_TO_ADD) != null) {
            filterArgs.put(FilterConfigKeys.HEADERS_TO_ADD, convertHeadersToAdd(config.get(PluginConfigKeys.HEADERS_TO_ADD)));
        }
        if (config.get(PluginConfigKeys.HEADERS_TO_REMOVE) != null) {
            filterArgs.put(FilterConfigKeys.HEADERS_TO_REMOVE, config.get(PluginConfigKeys.HEADERS_TO_REMOVE));
        }
        if (config.get(PluginConfigKeys.BODY_REGEX) != null) {
            filterArgs.put(FilterConfigKeys.BODY_REGEX, config.get(PluginConfigKeys.BODY_REGEX));
        }
        if (config.get(PluginConfigKeys.BODY_REPLACEMENT) != null) {
            filterArgs.put(FilterConfigKeys.BODY_REPLACEMENT, config.get(PluginConfigKeys.BODY_REPLACEMENT));
        }

        return FilterDefinition.builder()
                .name("ResponseRewriteFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertHeadersToAdd(Object value) {
        if (value instanceof Map) {
            return (Map<String, String>) value;
        }
        if (value instanceof List) {
            Map<String, String> map = new HashMap<>();
            for (Object item : (List<?>) value) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    Object key = m.get("key");
                    Object val = m.get("value");
                    if (key != null && !key.toString().isEmpty()) {
                        map.put(key.toString(), val != null ? val.toString() : "");
                    }
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); }
            catch (NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }

    private long toLong(Object value, long defaultValue) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); }
            catch (NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(Object configObj) {
        if (configObj == null) {
            return new HashMap<>();
        }
        if (configObj instanceof Map) {
            return (Map<String, Object>) configObj;
        }
        if (configObj instanceof String) {
            try {
                return OBJECT_MAPPER.readValue((String) configObj, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse config JSON: {}", configObj, e);
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private void notifyListeners(ConfigChangedEvent event) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onRouteConfigChanged(event);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener, e);
            }
        }
    }
}
