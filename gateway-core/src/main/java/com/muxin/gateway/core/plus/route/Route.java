package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.predicate.Predicate;

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

    Long getConnectionTimeout();

    Long getRequestTimeout();

    Long getTotalTimeout();

    Long getReadTimeout();

    Long getWriteTimeout();

    default boolean isTimeoutEnabled() {
        return true;
    }

    default Long getTimeout(TimeoutType type) {
        switch (type) {
            case CONNECTION:
                return getConnectionTimeout();
            case REQUEST:
                return getRequestTimeout();
            case TOTAL:
                return getTotalTimeout();
            case READ:
                return getReadTimeout();
            case WRITE:
                return getWriteTimeout();
            default:
                return null;
        }
    }
}
