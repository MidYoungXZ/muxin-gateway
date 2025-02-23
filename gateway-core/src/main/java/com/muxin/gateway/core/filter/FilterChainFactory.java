package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 定义了一个过滤器链工厂接口。该接口用于根据传入的 {@link ServerWebExchange} 对象构建过滤器链。
 * 实现该接口的类可以根据请求信息创建具体的过滤器链。
 *
 * @author Administrator
 * @date 2024/11/20 16:30
 */
public interface FilterChainFactory {

    /**
     * 根据传入的 {@link ServerWebExchange} 对象构建过滤器链。
     *
     * @param serverWebExchange 当前的 {@link ServerWebExchange} 对象
     * @return 构建的 {@link GatewayFilterChain} 对象
     */
    GatewayFilterChain buildFilterChain(ServerWebExchange serverWebExchange);
}
