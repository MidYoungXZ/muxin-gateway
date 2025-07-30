package com.muxin.gateway.core.route;

import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Collections.synchronizedMap;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 15:10
 */
@Data
public class InMemoryRouteDefinitionRepository implements RouteDefinitionRepository {

    private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<>());


    public InMemoryRouteDefinitionRepository(Collection<RouteDefinition> routes) {
        for (RouteDefinition route : routes) {
            this.routes.put(route.getId(), route);
        }
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
    public void removeByUniqueCode(String id) {
        if (id != null) {
            routes.remove(id);
        }
    }

    @Override
    public RouteDefinition findByUniqueCode(String id) {
        return id != null ? routes.get(id) : null;
    }

    @Override
    public Collection<RouteDefinition> findAll() {
        return routes.values();
    }
}
