package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:10
 */
public interface FilterChain {

    void filter(ServerWebExchange exchange);

}
