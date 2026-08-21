package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.filter.Filter;
import com.muxin.gateway.core.route.filter.FilterType;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.route.predicate.Predicate;

import java.util.Comparator;
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

    default boolean hasTimeout(TimeoutType type) {
        return false;
    }

    default int getStripPrefixCount() {
        return 0;
    }

    default String stripPrefix(String path) {
        return path;
    }

    default List<Filter> getPreFilters() {
        return getFilters().stream()
                .filter(f -> f.getType() == FilterType.PRE && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .toList();
    }

    default List<Filter> getPostFilters() {
        return getFilters().stream()
                .filter(f -> f.getType() == FilterType.POST && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .toList();
    }
}
