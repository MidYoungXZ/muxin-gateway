package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 网关过滤器链接口
 * 
 * 定义过滤器链的执行接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface GatewayFilterChain {

    void filter(ServerWebExchange exchange);

}
