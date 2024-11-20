package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 09:37
 */
public interface GatewayFilterChain {

    void  filter(ServerWebExchange exchange);

}
