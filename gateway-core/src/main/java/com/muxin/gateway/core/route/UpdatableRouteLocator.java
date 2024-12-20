package com.muxin.gateway.core.route;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/12/19 16:03
 */
public class UpdatableRouteLocator implements RouteLocator {

    RouteLocator delegate;


    @Override
    public List<RouteRule> getRoutes(String path) {
        return List.of();
    }
}
