package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 服务发现路由目标实现
 * 专注于服务发现类型的业务逻辑
 *
 * @author muxin
 */
@Data
@Builder
@Slf4j
public class DiscoveryRouteTarget implements RouteTarget {
    
    /**
     * 目标协议
     */
    private final Protocol protocol;
    
    /**
     * 服务名称
     */
    private final String serviceName;
    
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
        // 服务发现类型的地址由服务注册中心动态提供
        // 这里返回空列表，实际地址通过服务发现获取
        return List.of();
    }
    
    @Override
    public LoadBalanceStrategy loadBalanceStrategy() {
        // 服务发现目标由RouteConfigConverter根据loadBalanceDefinition进行负载均衡
        return null;
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context) {
        // 目标选择逻辑由RouteConfigConverter和服务发现组件处理
        throw new UnsupportedOperationException("目标选择由RouteConfigConverter和服务发现组件处理");
    }
    
    /**
     * 获取服务名称
     */
    public String getServiceName() {
        return serviceName;
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
     * 检查是否为服务发现类型
     */
    public boolean isDiscoveryType() {
        return true;
    }
    
    /**
     * 获取权重来源
     */
    public String getWeightSource() {
        return loadBalanceDefinition != null ? loadBalanceDefinition.getWeightSource() : "EQUAL";
    }
    
    /**
     * 获取权重元数据键名
     */
    public String getWeightMetadataKey() {
        return loadBalanceDefinition != null ? loadBalanceDefinition.getWeightMetadataKey() : null;
    }
} 