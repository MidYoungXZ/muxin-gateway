package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 09:36
 */
public interface RouteFilter extends Ordered {

    void filter(ServerWebExchange exchange);

    FilterTypeEnum filterType();

}
