package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.RandomLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.RoundRobinLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.WeightedRoundRobinLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.LeastConnectionsLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.HttpEndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CONFIG类型路由目标工厂
 * 负责创建CONFIG类型的RouteTarget实例
 *
 * @author muxin
 */
@Slf4j
public class ConfigRouteTargetFactory implements RouteTargetFactory {
    
    @Override
    public ServiceType getSupportedType() {
        return ServiceType.CONFIG;
    }
    
    @Override
    public RouteTarget createRouteTarget(RouteTargetDefinition definition) {
        log.debug("创建CONFIG类型路由目标: {}", definition.getServiceId());
        
        // 验证配置（内部验证）
        validateConfig(definition);
        
        // 转换协议
        Protocol protocol = convertProtocol(definition);
        
        // 转换地址列表
        List<EndpointAddress> addresses = convertAddresses(definition, protocol);
        
        // 创建负载均衡策略
        LoadBalanceStrategy strategy = createLoadBalanceStrategy(definition);
        
        // 创建并返回RouteTarget
        return new ConfigRouteTarget(
            definition.getServiceId(),
            definition.getServiceName(),
            protocol,
            addresses,
            strategy,
            definition.getConfig()
        );
    }
    
    @Override
    public void validateConfig(RouteTargetDefinition definition) {
        log.debug("验证CONFIG类型配置: {}", definition.getServiceId());
        
        // 基础验证
        if (definition.getServiceType() != ServiceType.CONFIG) {
            throw new IllegalArgumentException("服务类型必须是CONFIG");
        }
        
        // CONFIG类型特定验证
        validateConfigTypeDefinition(definition);
    }
    
    /**
     * 验证CONFIG类型的特定要求
     */
    private void validateConfigTypeDefinition(RouteTargetDefinition definition) {
        // 1. 必须有addresses
        if (definition.getAddresses() == null || definition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置addresses");
        }
        
        // 2. 验证每个地址
        for (int i = 0; i < definition.getAddresses().size(); i++) {
            AddressDefinition addressDef = definition.getAddresses().get(i);
            if (addressDef == null) {
                throw new IllegalArgumentException("addresses[" + i + "]不能为空");
            }
            
            // 必须是静态地址
            if (!addressDef.isStaticAddress()) {
                throw new IllegalArgumentException(
                    "CONFIG类型服务只支持静态地址(http://或https://)，当前地址: " + addressDef.getUri()
                );
            }
            
            // 验证地址格式
            try {
                addressDef.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException("addresses[" + i + "]配置无效: " + e.getMessage(), e);
            }
        }
        
        // 3. 验证负载均衡配置
        validateLoadBalanceConfig(definition);
    }
    
    /**
     * 验证负载均衡配置
     */
    private void validateLoadBalanceConfig(RouteTargetDefinition definition) {
        if (definition.getLoadBalance() != null) {
            String strategy = definition.getLoadBalance().getStrategy();
            if (strategy != null && !isSupportedStrategy(strategy)) {
                throw new IllegalArgumentException("不支持的负载均衡策略: " + strategy);
            }
        }
    }
    
    /**
     * 检查是否为支持的负载均衡策略
     */
    private boolean isSupportedStrategy(String strategy) {
        return "ROUND_ROBIN".equalsIgnoreCase(strategy) ||
               "RANDOM".equalsIgnoreCase(strategy) ||
               "WEIGHTED_ROUND_ROBIN".equalsIgnoreCase(strategy) ||
               "LEAST_CONNECTIONS".equalsIgnoreCase(strategy);
    }
    
    /**
     * 转换协议配置
     */
    private Protocol convertProtocol(RouteTargetDefinition definition) {
        try {
            return definition.getSupportProtocol().toProtocol();
        } catch (Exception e) {
            throw new IllegalArgumentException("协议转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换地址列表
     */
    private List<EndpointAddress> convertAddresses(RouteTargetDefinition definition, Protocol protocol) {
        List<EndpointAddress> addresses = new ArrayList<>();
        
        for (AddressDefinition addressDef : definition.getAddresses()) {
            try {
                EndpointAddress address = convertAddress(addressDef, protocol);
                addresses.add(address);
                
                log.debug("转换地址: {} -> {}", addressDef.getUri(), address.toUri());
                
            } catch (Exception e) {
                log.error("转换地址失败: {}", addressDef.getUri(), e);
                throw new IllegalArgumentException("地址转换失败: " + addressDef.getUri(), e);
            }
        }
        
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("没有有效的地址配置");
        }
        
        log.info("CONFIG服务 {} 转换地址完成，共 {} 个地址", 
                definition.getServiceName(), addresses.size());
        
        return addresses;
    }
    
    /**
     * 转换单个地址
     */
    private EndpointAddress convertAddress(AddressDefinition addressDef, Protocol protocol) {
        // 目前主要支持HTTP协议
        // TODO: 后续可以根据protocol类型创建不同的EndpointAddress实现
        
        // 使用URI构造函数，权重和元数据信息保存在AddressDefinition中
        // 通过负载均衡策略来处理权重信息
        return new HttpEndpointAddress(addressDef.getUri());
    }
    
    /**
     * 创建负载均衡策略
     */
    private LoadBalanceStrategy createLoadBalanceStrategy(RouteTargetDefinition definition) {
        String strategyName = "ROUND_ROBIN"; // 默认策略
        
        if (definition.getLoadBalance() != null && 
            definition.getLoadBalance().getStrategy() != null) {
            strategyName = definition.getLoadBalance().getStrategy().toUpperCase();
        }
        
        LoadBalanceStrategy strategy = createStrategyByName(strategyName);
        
        log.debug("为CONFIG服务 {} 创建负载均衡策略: {}", 
                definition.getServiceName(), strategy.getStrategyName());
        
        return strategy;
    }
    
    /**
     * 根据策略名称创建策略实例
     */
    private LoadBalanceStrategy createStrategyByName(String strategyName) {
        return switch (strategyName.toUpperCase()) {
            case "ROUND_ROBIN" -> new RoundRobinLoadBalanceStrategy();
            case "RANDOM" -> new RandomLoadBalanceStrategy();
            case "WEIGHTED_ROUND_ROBIN" -> new WeightedRoundRobinLoadBalanceStrategy();
            case "LEAST_CONNECTIONS" -> new LeastConnectionsLoadBalanceStrategy();
            default -> {
                log.warn("未知的负载均衡策略: {}, 使用默认策略: ROUND_ROBIN", strategyName);
                yield new RoundRobinLoadBalanceStrategy();
            }
        };
    }
    
    @Override
    public String toString() {
        return "ConfigRouteTargetFactory{supportedType=" + getSupportedType() + "}";
    }
} 