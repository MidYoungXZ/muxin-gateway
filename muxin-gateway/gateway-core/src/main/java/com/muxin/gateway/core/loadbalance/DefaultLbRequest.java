package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 15:29
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
