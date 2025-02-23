package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 默认负载均衡请求实现类，封装了与负载均衡相关的请求上下文信息。
 *
 * @author Administrator
 * @date 2025/1/10 15:29
 */
public class DefaultLbRequest implements LbRequest<ServerWebExchange> {

    // 请求上下文信息
    private final ServerWebExchange context; // 请求上下文信息

    /**
     * 构造函数，初始化请求上下文信息。
     *
     * @param context 请求上下文信息
     */
    public DefaultLbRequest(ServerWebExchange context) {
        this.context = context;
    }

    /**
     * 获取请求上下文信息。
     *
     * @return 请求上下文信息
     */
    @Override
    public ServerWebExchange getContext() {
        return context;
    }

}