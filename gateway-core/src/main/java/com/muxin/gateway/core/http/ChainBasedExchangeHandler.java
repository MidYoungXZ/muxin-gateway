package com.muxin.gateway.core.http;

import com.muxin.gateway.core.filter.GatewayFilter;
import com.muxin.gateway.core.filter.GatewayFilterChain;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
public class ChainBasedExchangeHandler implements ExchangeHandler {



    private final GatewayFilterChain gatewayFilterChain;

    public ChainBasedExchangeHandler(List<GatewayFilter> filters) {
        if (filters == null || filters.isEmpty()) {

        }



        this.gatewayFilterChain = new DefaultGatewayFilterChain(filters);
    }

    @Override
    public void handle(ServerWebExchange exchange) {
        gatewayFilterChain.filter(exchange);
    }






    public static class DefaultGatewayFilterChain implements GatewayFilterChain {

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


}
