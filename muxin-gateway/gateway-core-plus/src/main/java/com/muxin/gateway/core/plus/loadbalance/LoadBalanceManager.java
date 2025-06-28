package com.muxin.gateway.core.plus.loadbalance;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;
import com.muxin.gateway.core.plus.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.RequestContext;

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
                                 RequestContext context);
} 