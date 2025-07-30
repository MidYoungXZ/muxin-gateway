package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:10
 */
public interface GatewayFilterChain {

    void filter(ServerWebExchange exchange);

}
