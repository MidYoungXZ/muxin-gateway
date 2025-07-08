package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.node.EndpointAddress;
import com.muxin.gateway.core.plus.node.HttpEndpointAddress;
import com.muxin.gateway.core.plus.node.health.HealthCheckConfig;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 增强的路由目标实现
 * 支持STATIC和DISCOVERY两种类型
 *
 * @author muxin
 */
@Data
@Builder
@Slf4j
public class EnhancedRouteTarget implements RouteTarget {
    
    /**
     * 目标类型
     */
    private TargetType type;
    
    /**
     * 出站协议配置
     */
    private ProtocolConfig outboundProtocol;
    
    /**
     * 地址配置列表
     */
    private List<AddressConfig> addresses;
    
    /**
     * 负载均衡配置
     */
    private LoadBalanceConfig loadBalance;
    
    /**
     * 健康检查配置
     */
    private HealthCheckConfig healthCheckConfig;
    
    /**
     * 扩展配置
     */
    private Map<String, Object> targetConfig;
    
    @Override
    public Protocol getTargetProtocol() {
        return outboundProtocol != null ? outboundProtocol.toProtocol() : null;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        if (addresses == null) {
            return List.of();
        }
        
        return addresses.stream()
                .filter(addr -> addr.isStaticAddress()) // 只返回静态地址
                .map(this::toEndpointAddress)
                .collect(Collectors.toList());
    }
    
    @Override
    public LoadBalanceStrategy loadBalanceStrategy() {
        // 这里返回null，实际的策略会由LoadBalanceManager根据配置创建
        return null;
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return targetConfig;
    }
    
    @Override
    public HealthCheckConfig getHealthCheckConfig() {
        return healthCheckConfig;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context) {
        // 由LoadBalanceManager处理目标选择
        throw new UnsupportedOperationException("目标选择由LoadBalanceManager处理");
    }
    
    /**
     * 获取服务名称（仅适用于DISCOVERY类型）
     */
    public String getServiceName() {
        if (type != TargetType.DISCOVERY) {
            throw new IllegalStateException("只有DISCOVERY类型才能获取服务名称");
        }
        
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalStateException("DISCOVERY类型必须配置地址");
        }
        
        AddressConfig address = addresses.get(0);
        if (!address.isDiscoveryAddress()) {
            throw new IllegalStateException("DISCOVERY类型必须使用lb://协议");
        }
        
        return address.getServiceName();
    }
    
    /**
     * 验证配置
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("目标类型不能为空");
        }
        
        if (outboundProtocol == null) {
            throw new IllegalArgumentException("出站协议不能为空");
        }
        
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址配置不能为空");
        }
        
        // 验证地址配置
        for (AddressConfig address : addresses) {
            address.validate();
        }
        
        // DISCOVERY类型特殊验证
        if (type == TargetType.DISCOVERY) {
            validateDiscoveryConfig();
        }
        
        // STATIC类型特殊验证
        if (type == TargetType.STATIC) {
            validateStaticConfig();
        }
    }
    
    /**
     * 验证服务发现配置
     */
    private void validateDiscoveryConfig() {
        if (addresses.size() != 1) {
            throw new IllegalArgumentException("DISCOVERY类型只允许配置一个服务地址");
        }
        
        AddressConfig address = addresses.get(0);
        if (!address.isDiscoveryAddress()) {
            throw new IllegalArgumentException("DISCOVERY类型必须使用lb://协议");
        }
        
        if (address.getWeight() != null && address.getWeight() != 100) {
            throw new IllegalArgumentException("DISCOVERY类型的地址不支持设置权重");
        }
    }
    
    /**
     * 验证静态配置
     */
    private void validateStaticConfig() {
        for (AddressConfig address : addresses) {
            if (!address.isStaticAddress()) {
                throw new IllegalArgumentException("STATIC类型必须使用http://或https://协议");
            }
        }
    }
    
    /**
     * 转换为EndpointAddress
     */
    private EndpointAddress toEndpointAddress(AddressConfig addressConfig) {
        try {
            return new HttpEndpointAddress(addressConfig.getUri());
        } catch (Exception e) {
            log.error("转换地址配置失败: {}", addressConfig.getUri(), e);
            throw new IllegalArgumentException("无效的地址格式: " + addressConfig.getUri(), e);
        }
    }
    
    /**
     * 检查是否为静态类型
     */
    public boolean isStatic() {
        return type == TargetType.STATIC;
    }
    
    /**
     * 检查是否为服务发现类型
     */
    public boolean isDiscovery() {
        return type == TargetType.DISCOVERY;
    }
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        return loadBalance != null ? loadBalance.getStrategy() : "ROUND_ROBIN";
    }
    
    /**
     * 获取负载均衡配置
     */
    public LoadBalanceConfig getLoadBalanceConfig() {
        return loadBalance;
    }
} 