package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 路由过滤器接口
 * 
 * 定义路由过滤器的通用接口，扩展Ordered接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface RouteFilter extends Ordered {

    void filter(ServerWebExchange exchange);

    FilterTypeEnum filterType();

}
