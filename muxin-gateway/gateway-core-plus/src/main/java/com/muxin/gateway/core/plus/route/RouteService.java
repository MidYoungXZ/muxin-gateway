package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.List;
import java.util.Map;

/**
 * 路由目标接口
 * 路由的后端服务目标抽象
 *
 * @author muxin
 */
public interface RouteService {

    /**
     * 获取服务定义配置
     */
    ServiceDefinition serviceDefinition();
    /**
     * 目标协议
     */
    Protocol supportProtocol();
    
    /**
     * 目标地址列表
     */
    List<EndpointAddress> getTargetAddresses();
    
    /**
     * 负载均衡策略
     */
    LoadBalanceStrategy loadBalanceStrategy();
    
    /**
     * 目标配置
     */
    Map<String, Object> getTargetConfig();
    
    /**
     * 选择目标地址
     */
    EndpointAddress selectTarget(RequestContext context);
} 