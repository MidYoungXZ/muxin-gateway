package com.muxin.gateway.core.route;

import com.muxin.gateway.core.filter.Filter404;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class RouteRule {

    private String id;

    private URI uri;

    private int order;

    private List<RouteRuleFilter> routeRuleFilters;

    private RoutePredicate predicate;

    private Map<String, Object> metadata;

    public final static RouteRule ROUTE_404;

    static {
        ArrayList<RouteRuleFilter> filters = new ArrayList<>(1);
        filters.add(new Filter404());
        ROUTE_404 = RouteRule.builder()
                .id("404")
                .uri(URI.create("/"))
                .order(1)
                .routeRuleFilters(filters)
                .build();
    }

}
