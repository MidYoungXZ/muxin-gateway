package com.muxin.gateway.core.route;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 11:10
 */
public interface RouteDefinitionWriter {

    void save(RouteDefinition route);

    void delete(String routeId);

}
