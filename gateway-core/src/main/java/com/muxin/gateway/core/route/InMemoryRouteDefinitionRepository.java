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
    public void onApplicationEvent(RefreshRoutesEvent event) {

    }


    @Override
    public RouteDefinition save(RouteDefinition entity) {
        if (entity != null && entity.getId() != null) {
            routes.put(entity.getId(), entity);
            return entity;
        }
        return null;
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            routes.remove(id);
        }
    }

    @Override
    public RouteDefinition findById(String id) {
        return id != null ? routes.get(id) : null;
    }

    @Override
    public Iterable<RouteDefinition> findAll() {
        return routes.values();
    }
}
