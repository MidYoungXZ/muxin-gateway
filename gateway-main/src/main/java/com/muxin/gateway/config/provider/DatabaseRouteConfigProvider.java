package com.muxin.gateway.config.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.mapper.RoutePluginMapper;
import com.muxin.gateway.core.config.provider.ConfigChangedEvent;
import com.muxin.gateway.core.config.provider.ConfigChangeListener;
import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.route.RouteDefinition;
import com.muxin.gateway.core.route.filter.FilterDefinition;
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
                    .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
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
        List<Map<String, Object>> predicateMaps = routePredicateMapper.findPredicatesByRouteId(routeId);
        List<PredicateDefinition> predicates = new ArrayList<>();

        for (Map<String, Object> map : predicateMaps) {
            String predicateType = (String) map.get("predicateType");
            Map<String, Object> config = parseConfig(map.get("args"));

            PredicateDefinition predicate = PredicateDefinition.builder()
                    .name(predicateType)
                    .args(config != null ? config : new HashMap<>())
                    .build();
            predicates.add(predicate);
        }

        return predicates;
    }

    private List<FilterDefinition> loadFiltersFromPlugins(Long routeId) {
        List<Map<String, Object>> pluginMaps = routePluginMapper.findPluginsByRouteId(routeId);
        List<FilterDefinition> filters = new ArrayList<>();

        for (Map<String, Object> map : pluginMaps) {
            String pluginName = (String) map.get("plugin_name");
            String pluginType = (String) map.get("plugin_type");
            Map<String, Object> routeConfig = parseConfig(map.get("config"));
            Map<String, Object> defaultConfig = parseConfig(map.get("default_config"));
            Object priorityOverride = map.get("priority_override");
            Object defaultPriority = map.get("default_priority");
            Boolean pluginEnabled = map.get("enabled") instanceof Number
                    ? ((Number) map.get("enabled")).intValue() != 0
                    : Boolean.TRUE.equals(map.get("enabled"));

            if (!pluginEnabled) {
                continue;
            }

            Map<String, Object> effectiveConfig = mergeConfig(defaultConfig, routeConfig);
            int order = priorityOverride instanceof Number
                    ? ((Number) priorityOverride).intValue()
                    : (defaultPriority instanceof Number ? ((Number) defaultPriority).intValue() : 0);

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
        Object rate = config.get("rate");
        if (rate != null) {
            filterArgs.put("replenishRate", toInt(rate, 10));
        }
        Object burst = config.get("burst");
        if (burst != null) {
            filterArgs.put("burstCapacity", toInt(burst, 20));
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
        Object failureThreshold = config.get("failureThreshold");
        if (failureThreshold != null) {
            filterArgs.put("failureRateThreshold", toInt(failureThreshold, 50));
        }
        Object timeout = config.get("timeout");
        if (timeout != null) {
            filterArgs.put("waitDurationInOpenState", toLong(timeout, 60000));
        }
        Object successThreshold = config.get("successThreshold");
        if (successThreshold != null) {
            filterArgs.put("ringBufferSize", toInt(successThreshold, 100));
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
        filterArgs.put("allowOrigins", config.getOrDefault("allowOrigins", "*"));
        filterArgs.put("allowMethods", config.getOrDefault("allowMethods", "*"));
        filterArgs.put("allowHeaders", config.getOrDefault("allowHeaders", "*"));
        filterArgs.put("allowCredentials", config.getOrDefault("allowCredentials", false));
        filterArgs.put("maxAge", config.getOrDefault("maxAge", 3600));

        return FilterDefinition.builder()
                .name("CorsFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
    }

    private FilterDefinition createTimeoutFilter(Map<String, Object> config, int order) {
        Map<String, Object> filterArgs = new HashMap<>();
        filterArgs.put("connectTimeout", config.getOrDefault("connectTimeout", 5000));
        filterArgs.put("responseTimeout", config.getOrDefault("responseTimeout", 30000));

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
        
        if (config.get("pathRegex") != null) {
            filterArgs.put("pathRegex", config.get("pathRegex"));
        }
        if (config.get("pathReplacement") != null) {
            filterArgs.put("pathReplacement", config.get("pathReplacement"));
        }
        if (config.get("headersToAdd") != null) {
            filterArgs.put("headersToAdd", config.get("headersToAdd"));
        }
        if (config.get("headersToRemove") != null) {
            filterArgs.put("headersToRemove", config.get("headersToRemove"));
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
        
        if (config.get("headersToAdd") != null) {
            filterArgs.put("headersToAdd", config.get("headersToAdd"));
        }
        if (config.get("headersToRemove") != null) {
            filterArgs.put("headersToRemove", config.get("headersToRemove"));
        }
        if (config.get("bodyRegex") != null) {
            filterArgs.put("bodyRegex", config.get("bodyRegex"));
        }
        if (config.get("bodyReplacement") != null) {
            filterArgs.put("bodyReplacement", config.get("bodyReplacement"));
        }

        return FilterDefinition.builder()
                .name("ResponseRewriteFilter")
                .args(filterArgs)
                .order(order)
                .enabled(true)
                .build();
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
