package com.muxin.gateway.core.config.provider;

import com.muxin.gateway.core.route.RouteDefinition;

import java.util.List;
import java.util.Optional;

public interface RouteConfigProvider {

    List<RouteDefinition> getRoutes();

    Optional<RouteDefinition> getRoute(String routeId);

    void refresh();

    void addChangeListener(ConfigChangeListener listener);

    void removeChangeListener(ConfigChangeListener listener);

    String getSource();
}