package com.muxin.gateway.core.route;

import com.muxin.gateway.core.http.ServerWebExchange;

@FunctionalInterface
public interface RoutePredicate {
    boolean test(ServerWebExchange exchange);
} 