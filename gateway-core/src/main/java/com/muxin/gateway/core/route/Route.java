package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.filter.Filter;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.route.predicate.Predicate;

import java.util.List;
import java.util.Map;

public interface Route {

    String getId();

    String getName();

    String getDescription();

    int getOrder();

    boolean isEnabled();

    List<Predicate> getPredicates();

    List<Filter> getFilters();

    RouteService getService();

    LoadBalanceStrategy getLoadBalanceStrategy();

    Map<String, Object> getMetadata();

    boolean matches(RequestContext context);

    void validate();

    default long getTimeout(TimeoutType type) {
        return TimeoutConfig.getDefault(type);
    }
}