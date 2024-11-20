package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:33
 */
public class DefaultGatewayFilterChain implements GatewayFilterChain {

    private final List<GatewayFilter> filters;

    public DefaultGatewayFilterChain(List<GatewayFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        for (GatewayFilter filter : filters) {
            filter.filter(exchange);
        }
    }

}
