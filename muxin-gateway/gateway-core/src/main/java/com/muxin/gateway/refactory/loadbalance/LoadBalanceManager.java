package com.muxin.gateway.refactory.loadbalance;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.LifeCycle;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.List;

/**
 * 负载均衡管理器接口
 *
 * @author muxin
 */
public interface LoadBalanceManager extends Repository<String, LoadBalanceStrategy>, LifeCycle {
    
    /**
     * 选择目标节点
     */
    EndpointAddress selectTarget(String serviceName, List<EndpointAddress> availableTargets,
                                 UniversalRequestContext context);
} 