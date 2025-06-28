package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.node.EndpointAddress;
import com.muxin.gateway.core.plus.node.health.HealthCheckConfig;

import java.util.List;
import java.util.Map;

/**
 * 路由目标接口
 *
 * @author muxin
 */
public interface RouteTarget {
    
    /**
     * 目标协议
     */
    Protocol getTargetProtocol();
    
    /**
     * 目标地址列表
     */
    List<EndpointAddress> getTargetAddresses();
    
    /**
     * 负载均衡策略
     */
    String getLoadBalanceStrategy();
    
    /**
     * 目标配置
     */
    Map<String, Object> getTargetConfig();
    
    /**
     * 健康检查配置
     */
    HealthCheckConfig getHealthCheckConfig();
    
    /**
     * 选择目标地址
     */
    EndpointAddress selectTarget(RequestContext context);
} 