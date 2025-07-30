package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.filter.PartFilter;
import com.muxin.gateway.core.route.filter.Filter404;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 路由规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRule {

    private String id;

    private URI uri;

    private int order;

    private List<PartFilter> routeFilters;

    private RoutePredicate predicate;

    private Map<String, Object> metadata;

    public final static RouteRule ROUTE_404;

    static {
        ArrayList<PartFilter> filters = new ArrayList<>(1);
        filters.add(Filter404.instance());
        ROUTE_404 = RouteRule.builder()
                .id("404")
                .uri(URI.create("/"))
                .order(1)
                .routeRuleFilters(filters)
                .build();
    }

    public static RouteRuleBuilder builder() {
        return new RouteRuleBuilder();
    }

    public static class RouteRuleBuilder {
        private String id;
        private URI uri;
        private RoutePredicate predicate;
        private List<PartFilter> routeFilters;
        private Map<String, Object> metadata;
        private int order;

        public RouteRuleBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RouteRuleBuilder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public RouteRuleBuilder predicate(RoutePredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public RouteRuleBuilder routeRuleFilters(List<PartFilter> routeFilters) {
            this.routeFilters = routeFilters;
            return this;
        }

        public RouteRuleBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public RouteRuleBuilder order(int order) {
            this.order = order;
            return this;
        }

        public RouteRule build() {
            RouteRule routeRule = new RouteRule();
            routeRule.id = this.id;
            routeRule.uri = this.uri;
            routeRule.predicate = this.predicate;
            routeRule.routeFilters = this.routeFilters;
            routeRule.metadata = this.metadata;
            routeRule.order = this.order;
            return routeRule;
        }
    }
}
