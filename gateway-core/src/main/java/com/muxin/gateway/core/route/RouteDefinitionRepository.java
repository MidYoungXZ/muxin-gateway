package com.muxin.gateway.core.route;

import java.util.Collection;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 11:10
 */
public interface RouteDefinitionRepository {

    Collection<RouteDefinition> getRouteDefinitions();

    void save(RouteDefinition route);

    void delete(String routeId);

}
