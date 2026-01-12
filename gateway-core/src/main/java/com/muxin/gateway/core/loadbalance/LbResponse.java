package com.muxin.gateway.core.loadbalance;

/**
 * 负载均衡响应接口
 * 
 * 定义负载均衡响应的通用接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface LbResponse<T> {

    boolean hasServer();

    T getServer();

}
