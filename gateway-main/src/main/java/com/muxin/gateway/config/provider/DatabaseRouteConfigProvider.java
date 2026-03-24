package com.muxin.gateway.config.provider;

import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.mapper.RouteFilterMapper;
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

    private final RouteMapper routeMapper;
    private final RoutePredicateMapper routePredicateMapper;
    private final RouteFilterMapper routeFilterMapper;
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private volatile List<RouteDefinition> cachedRoutes = new ArrayList<>();

    @Override
    public List<RouteDefinition> getRoutes() {
        if (cachedRoutes.isEmpty()) {
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

        List<FilterDefinition> filters = loadFilters(route.getId());
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
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) map.get("config");

            PredicateDefinition predicate = PredicateDefinition.builder()
                    .type(predicateType)
                    .config(config != null ? config : new HashMap<>())
                    .build();
            predicates.add(predicate);
        }

        return predicates;
    }

    private List<FilterDefinition> loadFilters(Long routeId) {
        List<Map<String, Object>> filterMaps = routeFilterMapper.findFiltersByRouteId(routeId);
        List<FilterDefinition> filters = new ArrayList<>();

        for (Map<String, Object> map : filterMaps) {
            String filterType = (String) map.get("filterType");
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) map.get("config");
            Object orderObj = map.get("order");
            int order = orderObj instanceof Number ? ((Number) orderObj).intValue() : 0;

            FilterDefinition filter = FilterDefinition.builder()
                    .type(filterType)
                    .config(config != null ? config : new HashMap<>())
                    .order(order)
                    .enabled(true)
                    .build();
            filters.add(filter);
        }

        return filters;
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