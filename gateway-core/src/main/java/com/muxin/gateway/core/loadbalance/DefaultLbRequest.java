package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 默认负载均衡请求类
 * 
 * 实现LbRequest接口，提供默认的负载均衡请求实现
 *
 * @author Administrator
 * @since 1.0.0
 */
public class DefaultLbRequest implements LbRequest<ServerWebExchange> {

    private final ServerWebExchange context;

    public DefaultLbRequest(ServerWebExchange context) {
        this.context = context;
    }

    @Override
    public ServerWebExchange getContext() {
        return context;
    }


}
