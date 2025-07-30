package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.ServiceInstance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:44
 */
public interface GatewayLoadBalance extends LoadBalance<ServiceInstance, ServerWebExchange>{

    String loadBalanceType();

}
