package com.muxin.gateway.core.route;

import com.muxin.gateway.core.event.RefreshRoutesEvent;
import lombok.Data;
import org.springframework.context.ApplicationListener;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 15:10
 */
@Data
public class InMemoryRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationListener<RefreshRoutesEvent> {

    private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<>());


    public InMemoryRouteDefinitionRepository(Collection<RouteDefinition> routes) {
        for (RouteDefinition route : routes) {
            this.routes.put(route.getId(), route);
        }
    }

    @Override
    public Collection<RouteDefinition> getRouteDefinitions() {
        Map<String, RouteDefinition> routesSafeCopy = new LinkedHashMap<>(routes);
        return routesSafeCopy.values();
    }

    @Override
    public void save(RouteDefinition route) {

    }

    @Override
    public void delete(String routeId) {

    }

    @Override
    public void onApplicationEvent(RefreshRoutesEvent event) {

    }


}
