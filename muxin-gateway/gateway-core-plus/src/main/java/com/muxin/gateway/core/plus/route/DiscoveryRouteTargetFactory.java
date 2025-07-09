package com.muxin.gateway.core.plus.route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 服务发现路由目标工厂
 * 负责创建服务发现类型的路由目标
 *
 * @author muxin
 */
@Slf4j
@Component
public class DiscoveryRouteTargetFactory implements RouteTargetFactory {
    
    @Override
    public RouteTarget createRouteTarget(RouteTargetDefinition definition) {
        validateConfig(definition);
        
        // 获取服务名称
        String serviceName = definition.getServiceName();
        
        return DiscoveryRouteTarget.builder()
                .protocol(definition.getOutboundProtocol().toProtocol())
                .serviceName(serviceName)
                .loadBalanceDefinition(definition.getLoadBalance())
                .config(definition.getConfig())
                .build();
    }
    
    @Override
    public TargetType getSupportedType() {
        return TargetType.DISCOVERY;
    }
    
    @Override
    public void validateConfig(RouteTargetDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("路由目标配置不能为空");
        }
        
        if (definition.getType() != TargetType.DISCOVERY) {
            throw new IllegalArgumentException("工厂类型不匹配，期望: DISCOVERY，实际: " + definition.getType());
        }
        
        if (definition.getOutboundProtocol() == null) {
            throw new IllegalArgumentException("出站协议配置不能为空");
        }
        
        if (definition.getAddresses() == null || definition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("服务发现目标必须配置服务地址");
        }
        
        if (definition.getAddresses().size() != 1) {
            throw new IllegalArgumentException("服务发现目标只允许配置一个服务地址");
        }
        
        // 验证地址格式
        AddressDefinition address = definition.getAddresses().get(0);
        address.validate();
        
        if (!address.isDiscoveryAddress()) {
            throw new IllegalArgumentException("服务发现目标必须使用 lb:// 协议，无效地址: " + address.getUri());
        }
        
        if (address.getWeight() != null && address.getWeight() != 100) {
            throw new IllegalArgumentException("服务发现地址不支持设置权重");
        }
        
        // 验证服务名称
        try {
            String serviceName = address.getServiceName();
            if (serviceName == null || serviceName.trim().isEmpty()) {
                throw new IllegalArgumentException("服务名称不能为空");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的服务名称: " + address.getUri(), e);
        }
        
        // 验证负载均衡配置
        if (definition.getLoadBalance() != null) {
            validateLoadBalanceConfig(definition);
        }
    }
    
    /**
     * 验证负载均衡配置
     */
    private void validateLoadBalanceConfig(RouteTargetDefinition definition) {
        var loadBalance = definition.getLoadBalance();
        String strategy = loadBalance.getStrategy();
        
        // 验证一致性哈希策略的配置
        if ("CONSISTENT_HASH".equalsIgnoreCase(strategy)) {
            if (loadBalance.getHashKey() == null || loadBalance.getHashKey().trim().isEmpty()) {
                throw new IllegalArgumentException("一致性哈希策略必须配置 hashKey");
            }
        }
        
        // 验证权重配置
        if (loadBalance.isWeightedStrategy()) {
            String weightSource = loadBalance.getWeightSource();
            if (weightSource == null) {
                throw new IllegalArgumentException("加权策略必须配置权重来源 (REGISTRY/EQUAL)");
            }
            
            if ("REGISTRY".equalsIgnoreCase(weightSource)) {
                if (loadBalance.getWeightMetadataKey() == null || loadBalance.getWeightMetadataKey().trim().isEmpty()) {
                    throw new IllegalArgumentException("权重来源为 REGISTRY 时必须配置 weightMetadataKey");
                }
            }
        }
    }
} 