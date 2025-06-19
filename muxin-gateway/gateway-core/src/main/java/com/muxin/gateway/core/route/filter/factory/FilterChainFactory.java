package com.muxin.gateway.core.route.filter.factory;

import com.muxin.gateway.core.route.filter.GatewayFilterChain;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:30
 */
public interface FilterChainFactory {

    GatewayFilterChain buildFilterChain(ServerWebExchange serverWebExchange);

}
