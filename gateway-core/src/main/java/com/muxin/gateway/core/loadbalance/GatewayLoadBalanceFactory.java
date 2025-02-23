package com.muxin.gateway.core.loadbalance;

/**
 * 定义了一个网关负载均衡工厂接口。
 * 该接口用于根据负载均衡类型获取相应的负载均衡策略。
 *
 * @author Administrator
 * @date 2025/1/16 16:29
 */
public interface GatewayLoadBalanceFactory {

    /**
     * 根据负载均衡类型获取相应的负载均衡策略。
     *
     * @param loadBalanceType 负载均衡类型
     * @return 对应的负载均衡策略
     */
    GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType);
}
