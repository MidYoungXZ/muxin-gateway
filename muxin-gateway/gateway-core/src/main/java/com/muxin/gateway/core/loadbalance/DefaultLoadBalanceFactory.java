package com.muxin.gateway.core.loadbalance;

import lombok.Data;

import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/16 16:42
 */
@Data
public class DefaultLoadBalanceFactory implements GatewayLoadBalanceFactory {

    private Map<String, GatewayLoadBalance> gatewayLoadBalanceMap;

    public DefaultLoadBalanceFactory() {
    }

    public DefaultLoadBalanceFactory(Map<String, GatewayLoadBalance> gatewayLoadBalanceMap) {
        this.gatewayLoadBalanceMap = gatewayLoadBalanceMap;
    }

    @Override
    public GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType) {
        return gatewayLoadBalanceMap != null ? gatewayLoadBalanceMap.get(loadBalanceType) : null;
    }

    public void setGatewayLoadBalanceMap(Map<String, GatewayLoadBalance> gatewayLoadBalanceMap) {
        this.gatewayLoadBalanceMap = gatewayLoadBalanceMap;
    }
}
