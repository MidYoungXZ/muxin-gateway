package com.muxin.gateway.core.loadbalance;

/**
 * 负载均衡请求接口
 * 
 * 定义负载均衡请求的通用接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface LbRequest<C> {

    default C getContext() {
        return null;
    }

}
