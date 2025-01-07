package com.muxin.gateway.core.route;

import com.muxin.gateway.core.filter.RouteRuleFilter;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
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
}
