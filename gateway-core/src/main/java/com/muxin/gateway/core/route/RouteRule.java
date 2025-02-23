package com.muxin.gateway.core.route;

import com.muxin.gateway.core.filter.Filter404;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 定义了一个路由规则类。
 * 该类用于存储路由的详细信息，包括ID、目标URI、顺序、过滤器列表、断言和元数据。
 *
 * @author Administrator
 * @date 2024/11/21 10:56
 */
@Data
@Builder
public class RouteRule {

    /**
     * 路由的唯一标识符。
     */
    private String id;

    /**
     * 路由的目标URI。
     */
    private URI uri;

    /**
     * 路由的顺序，用于排序。
     */
    private int order;

    /**
     * 路由规则的过滤器列表。
     */
    private List<RouteRuleFilter> routeRuleFilters;

    /**
     * 路由规则的断言，用于匹配请求。
     */
    private RoutePredicate predicate;

    /**
     * 路由的元数据映射。
     */
    private Map<String, Object> metadata;

    /**
     * 预定义的404路由规则，当没有匹配到任何路由时使用。
     */
    public final static RouteRule ROUTE_404;

    static {
        ArrayList<RouteRuleFilter> filters = new ArrayList<>(1);
        filters.add(Filter404.instance());
        ROUTE_404 = RouteRule.builder()
                .id("404")
                .uri(URI.create("/"))
                .order(1)
                .routeRuleFilters(filters)
                .build();
    }
}
