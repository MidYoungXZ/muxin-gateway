package com.muxin.gateway.core.loadbalance;

import java.util.Map;

/**
 * 默认的负载均衡工厂类，实现了 {@link GatewayLoadBalanceFactory} 接口。
 * 该类用于根据负载均衡类型获取相应的负载均衡策略。
 *
 * @author Administrator
 * @date 2025/1/16 16:42
 */
public class DefaultLoadBalanceFactory implements GatewayLoadBalanceFactory {

    private Map<String, GatewayLoadBalance> gatewayLoadBalanceMap;

    /**
     * 构造函数，用于初始化负载均衡工厂。
     *
     * @param gatewayLoadBalanceMap 负载均衡策略的映射表
     */
    public DefaultLoadBalanceFactory(Map<String, GatewayLoadBalance> gatewayLoadBalanceMap) {
        this.gatewayLoadBalanceMap = gatewayLoadBalanceMap;
    }

    /**
     * 根据负载均衡类型获取相应的负载均衡策略。
     *
     * @param loadBalanceType 负载均衡类型
     * @return 对应的负载均衡策略
     */
    @Override
    public GatewayLoadBalance getGatewayLoadBalance(String loadBalanceType) {
        return gatewayLoadBalanceMap.get(loadBalanceType);
    }
}
