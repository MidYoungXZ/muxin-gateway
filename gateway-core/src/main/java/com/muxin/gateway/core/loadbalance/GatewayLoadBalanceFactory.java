package com.muxin.gateway.core.loadbalance;

/**
 * 网关负载均衡工厂接口
 * 
 * 定义负载均衡器工厂的接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface GatewayLoadBalanceFactory {

    GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType);

}
