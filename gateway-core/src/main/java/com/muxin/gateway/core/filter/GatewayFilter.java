package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 09:36
 */
public interface GatewayFilter extends Ordered {

    void filter(ServerWebExchange exchange);

    FilterTypeEnum filterType();

}
