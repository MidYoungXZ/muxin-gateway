package com.muxin.gateway.core.loadbalance;

import lombok.Data;

import java.util.Map;

/**
 * 默认负载均衡工厂类
 * 
 * 实现GatewayLoadBalanceFactory接口，提供默认的负载均衡工厂实现
 *
 * @author Administrator
 * @since 1.0.0
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
