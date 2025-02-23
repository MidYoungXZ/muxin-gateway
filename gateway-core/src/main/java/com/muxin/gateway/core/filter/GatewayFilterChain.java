package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 定义了一个网关过滤器链接口。该接口用于处理HTTP请求的过滤链。
 * 实现该接口的类负责执行一系列过滤器，以处理传入的 {@link ServerWebExchange} 对象。
 *
 * @author Administrator
 * @date 2024/11/20 16:10
 */
public interface GatewayFilterChain {

    /**
     * 执行过滤器链中的过滤器。
     *
     * @param exchange 当前的 {@link ServerWebExchange} 对象
     */
    void filter(ServerWebExchange exchange);
}
