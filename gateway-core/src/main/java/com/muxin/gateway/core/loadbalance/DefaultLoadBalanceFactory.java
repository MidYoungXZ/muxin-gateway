package com.muxin.gateway.core.loadbalance;

import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/16 16:42
 */
public class DefaultLoadBalanceFactory implements GatewayLoadBalanceFactory{

    private Map<String, GatewayLoadBalance> gatewayLoadBalanceMap;

    @Override
    public GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType) {
        return gatewayLoadBalanceMap.get(loadBalanceType);
    }
}
