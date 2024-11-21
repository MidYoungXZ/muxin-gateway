package com.muxin.gateway.core.route;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.filter.GatewayFilter;
import com.muxin.gateway.core.predicate.RoutePredicate;
import lombok.Data;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 14:51
 */
@Data
public class RouteRule implements Ordered {

    private final String id;

    private final URI uri;

    private final int order;

    private final RoutePredicate  predicate;

    private final List<GatewayFilter> gatewayFilters;

    private final Map<String, Object> metadata;

}
