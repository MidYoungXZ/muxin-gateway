package com.muxin.gateway.core.loadbalance;

/**
 * 负载均衡接口
 * 
 * 定义负载均衡算法的通用接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface LoadBalance<S, C> {

    LbResponse<S> choose(LbRequest<C> request);

}
