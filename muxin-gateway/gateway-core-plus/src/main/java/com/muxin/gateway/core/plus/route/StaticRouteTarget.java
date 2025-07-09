package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 静态路由目标实现
 * 专注于静态地址配置的业务逻辑
 *
 * @author muxin
 */
@Data
@Builder
@Slf4j
public class StaticRouteTarget implements RouteTarget {
    
    /**
     * 目标协议
     */
    private final Protocol protocol;
    
    /**
     * 静态端点地址列表
     */
    private final List<EndpointAddress> endpoints;
    
    /**
     * 负载均衡定义
     */
    private final LoadBalanceDefinition loadBalanceDefinition;
    
    /**
     * 扩展配置
     */
    private final Map<String, Object> config;
    
    @Override
    public Protocol getTargetProtocol() {
        return protocol;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        return endpoints;
    }
    
    @Override
    public LoadBalanceStrategy loadBalanceStrategy() {
        // 静态目标由RouteConfigConverter根据loadBalanceDefinition进行负载均衡
        return null;
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context) {
        // 目标选择逻辑由RouteConfigConverter处理
        throw new UnsupportedOperationException("目标选择由RouteConfigConverter处理");
    }
    
    /**
     * 获取负载均衡定义
     */
    public LoadBalanceDefinition getLoadBalanceDefinition() {
        return loadBalanceDefinition;
    }
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        return loadBalanceDefinition != null ? loadBalanceDefinition.getStrategy() : "ROUND_ROBIN";
    }
    
    /**
     * 检查是否有可用的端点
     */
    public boolean hasAvailableEndpoints() {
        return endpoints != null && !endpoints.isEmpty();
    }
    
    /**
     * 获取端点数量
     */
    public int getEndpointCount() {
        return endpoints != null ? endpoints.size() : 0;
    }
} 