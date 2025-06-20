package com.muxin.gateway.refactory;

import java.util.List;

/**
 * 负载均衡管理器接口
 *
 * @author muxin
 */
public interface LoadBalanceManager {
    
    /**
     * 注册负载均衡策略
     */
    void registerStrategy(String name, LoadBalanceStrategy strategy);
    
    /**
     * 获取负载均衡策略
     */
    LoadBalanceStrategy getStrategy(String name);
    
    /**
     * 选择目标节点
     */
    EndpointAddress selectTarget(String serviceName, List<EndpointAddress> availableTargets, 
                                UniversalRequestContext context);
} 