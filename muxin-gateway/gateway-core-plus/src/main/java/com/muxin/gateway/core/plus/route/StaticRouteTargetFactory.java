package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.node.HttpEndpointAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 静态路由目标工厂
 * 负责创建静态类型的路由目标
 *
 * @author muxin
 */
@Slf4j
@Component
public class StaticRouteTargetFactory implements RouteTargetFactory {
    
    @Override
    public RouteTarget createRouteTarget(RouteTargetDefinition definition) {
        validateConfig(definition);
        
        // 转换地址配置为EndpointAddress
        List<EndpointAddress> endpoints = definition.getAddresses().stream()
                .filter(AddressDefinition::isStaticAddress)
                .map(this::toEndpointAddress)
                .collect(Collectors.toList());
        
        return StaticRouteTarget.builder()
                .protocol(definition.getOutboundProtocol().toProtocol())
                .endpoints(endpoints)
                .loadBalanceDefinition(definition.getLoadBalance())
                .config(definition.getConfig())
                .build();
    }
    
    @Override
    public TargetType getSupportedType() {
        return TargetType.STATIC;
    }
    
    @Override
    public void validateConfig(RouteTargetDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("路由目标配置不能为空");
        }
        
        if (definition.getType() != TargetType.STATIC) {
            throw new IllegalArgumentException("工厂类型不匹配，期望: STATIC，实际: " + definition.getType());
        }
        
        if (definition.getOutboundProtocol() == null) {
            throw new IllegalArgumentException("出站协议配置不能为空");
        }
        
        if (definition.getAddresses() == null || definition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("静态目标必须配置至少一个地址");
        }
        
        // 验证所有地址都是静态地址
        for (AddressDefinition address : definition.getAddresses()) {
            address.validate();
            if (!address.isStaticAddress()) {
                throw new IllegalArgumentException("静态目标只能使用 http:// 或 https:// 协议，无效地址: " + address.getUri());
            }
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
        
        // 验证加权策略的配置
        if (loadBalance.isWeightedStrategy()) {
            for (AddressDefinition address : definition.getAddresses()) {
                if (address.getWeight() == null || address.getWeight() <= 0) {
                    throw new IllegalArgumentException("加权负载均衡策略要求所有地址都配置正整数权重");
                }
            }
        }
    }
    
    /**
     * 转换地址配置为EndpointAddress
     */
    private EndpointAddress toEndpointAddress(AddressDefinition addressConfig) {
        try {
            return new HttpEndpointAddress(addressConfig.getUri());
        } catch (Exception e) {
            log.error("转换地址配置失败: {}", addressConfig.getUri(), e);
            throw new IllegalArgumentException("无效的地址格式: " + addressConfig.getUri(), e);
        }
    }
} 