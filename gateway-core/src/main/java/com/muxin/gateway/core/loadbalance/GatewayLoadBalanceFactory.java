package com.muxin.gateway.core.loadbalance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/16 16:29
 */
public interface GatewayLoadBalanceFactory {

    GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType);

}
