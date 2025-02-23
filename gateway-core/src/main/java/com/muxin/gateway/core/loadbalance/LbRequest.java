package com.muxin.gateway.core.loadbalance;

/**
 * 定义了一个负载均衡请求接口。
 * 该接口用于表示负载均衡请求，并提供获取上下文的方法。
 *
 * @author Administrator
 * @date 2025/1/9 17:26
 */
public interface LbRequest<C> {

    /**
     * 获取负载均衡请求的上下文。
     *
     * @return 负载均衡请求的上下文
     */
    default C getContext() {
        return null;
    }
}
