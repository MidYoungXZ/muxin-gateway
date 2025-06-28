package com.muxin.gateway.core.plus.loadbalance;

import com.muxin.gateway.core.plus.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.RequestContext;

import java.util.List;

/**
 * 负载均衡策略接口
 *
 * @author muxin
 */
public interface LoadBalanceStrategy {
    
    /**
     * 从可用地址列表中选择一个目标地址
     *
     * @param availableAddresses 可用地址列表
     * @param context 请求上下文
     * @return 选中的地址
     */
    EndpointAddress select(List<EndpointAddress> availableAddresses, RequestContext context);
    
    /**
     * 策略名称
     */
    String getName();
    
    /**
     * 更新地址健康状态
     */
    void updateHealthStatus(EndpointAddress address, boolean isHealthy);
    
    /**
     * 获取策略配置
     */
    Object getConfiguration();
} 