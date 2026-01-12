package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.ServiceInstance;

/**
 * 网关负载均衡接口
 * 
 * 定义网关负载均衡器的基本接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface GatewayLoadBalance extends LoadBalance<ServiceInstance, ServerWebExchange>{

    String loadBalanceType();

}
