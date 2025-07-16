package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.msg.Protocol;
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

/**
 * CONFIG类型路由目标工厂
 * 负责根据服务定义创建CONFIG类型的RouteTarget实例
 *
 * @author muxin
 */
@Slf4j
public class ConfigRouteServiceFactory implements RouteServiceFactory {
    
    @Override
    public ServiceType getSupportedType() {
        return ServiceType.CONFIG;
    }
    
    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("创建CONFIG类型路由目标: {}", serviceDefinition.getId());
        
        // 验证配置（内部验证）
        validateConfig(serviceDefinition);
        
        // 转换协议
        Protocol protocol = convertProtocol(serviceDefinition);
        
        // 转换地址列表
        List<EndpointAddress> addresses = convertAddresses(serviceDefinition, protocol);
        
        // 创建负载均衡策略
        LoadBalanceStrategy strategy = createLoadBalanceStrategy(serviceDefinition);
        
        // 创建并返回RouteTarget
        return new ConfigRouteService(
            serviceDefinition,
            protocol,
            addresses,
            strategy,
            serviceDefinition.getConfig()
        );
    }
    
    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("验证CONFIG类型配置: {}", serviceDefinition.getId());
        
        // 基础验证
        if (serviceDefinition.getType() != ServiceType.CONFIG) {
            throw new IllegalArgumentException("服务类型必须是CONFIG");
        }
        
        // CONFIG类型特定验证
        validateConfigTypeDefinition(serviceDefinition);
    }
    
    /**
     * 验证CONFIG类型的特定要求
     */
    private void validateConfigTypeDefinition(ServiceDefinition serviceDefinition) {
        // 1. 必须有addresses
        if (serviceDefinition.getAddresses() == null || serviceDefinition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置addresses");
        }
        
        // 2. 验证每个地址
        for (int i = 0; i < serviceDefinition.getAddresses().size(); i++) {
            AddressDefinition addressDef = serviceDefinition.getAddresses().get(i);
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
        validateLoadBalanceConfig(serviceDefinition);
    }
    
    /**
     * 验证负载均衡配置
     */
    private void validateLoadBalanceConfig(ServiceDefinition serviceDefinition) {
        if (serviceDefinition.getLoadBalance() != null) {
            String strategy = serviceDefinition.getLoadBalance().getStrategy();
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
    private Protocol convertProtocol(ServiceDefinition serviceDefinition) {
        try {
            return serviceDefinition.getSupportProtocol().toProtocol();
        } catch (Exception e) {
            throw new IllegalArgumentException("协议转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换地址列表
     */
    private List<EndpointAddress> convertAddresses(ServiceDefinition serviceDefinition, Protocol protocol) {
        List<EndpointAddress> addresses = new ArrayList<>();
        
        for (AddressDefinition addressDef : serviceDefinition.getAddresses()) {
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
                serviceDefinition.getName(), addresses.size());
        
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
    private LoadBalanceStrategy createLoadBalanceStrategy(ServiceDefinition serviceDefinition) {
        String strategyName = "ROUND_ROBIN"; // 默认策略
        
        if (serviceDefinition.getLoadBalance() != null && 
            serviceDefinition.getLoadBalance().getStrategy() != null) {
            strategyName = serviceDefinition.getLoadBalance().getStrategy().toUpperCase();
        }
        
        LoadBalanceStrategy strategy = createStrategyByName(strategyName);
        
        log.debug("为CONFIG服务 {} 创建负载均衡策略: {}", 
                serviceDefinition.getName(), strategy.getStrategyName());
        
        return strategy;
    }
    
    /**
     * 根据策略名称创建策略实例
     */
    private LoadBalanceStrategy createStrategyByName(String strategyName) {
        switch (strategyName.toUpperCase()) {
            case "ROUND_ROBIN":
                return new RoundRobinLoadBalanceStrategy();
            case "RANDOM":
                return new RandomLoadBalanceStrategy();
            case "WEIGHTED_ROUND_ROBIN":
                return new WeightedRoundRobinLoadBalanceStrategy();
            case "LEAST_CONNECTIONS":
                return new LeastConnectionsLoadBalanceStrategy();
            default:
                log.warn("未知的负载均衡策略: {}, 使用默认策略: ROUND_ROBIN", strategyName);
                return new RoundRobinLoadBalanceStrategy();
        }
    }
    
    @Override
    public String toString() {
        return "ConfigRouteTargetFactory{supportedType=" + getSupportedType() + "}";
    }
} 