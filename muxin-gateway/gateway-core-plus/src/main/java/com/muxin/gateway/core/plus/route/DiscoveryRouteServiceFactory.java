package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.ServiceRegistry;
import com.muxin.gateway.core.plus.msg.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.RandomLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.RoundRobinLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.WeightedRoundRobinLoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.loadbalance.LeastConnectionsLoadBalanceStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * DISCOVERY类型路由目标工厂
 * 负责根据服务定义创建和验证基于服务发现的路由目标
 *
 * @author muxin
 */
@Slf4j
public class DiscoveryRouteServiceFactory implements RouteServiceFactory {
    
    private final ServiceRegistry serviceRegistry;
    
    public DiscoveryRouteServiceFactory(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceDiscovery不能为空");
    }
    
    @Override
    public ServiceType getSupportedType() {
        return ServiceType.DISCOVERY;
    }
    
    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("开始创建DISCOVERY路由目标: {}", serviceDefinition.getId());
        
        // 验证定义
        validateConfig(serviceDefinition);
        
        try {
            // 获取协议
            Protocol protocol = serviceDefinition.getSupportProtocol().toProtocol();
            
            // 创建负载均衡策略
            LoadBalanceStrategy loadBalanceStrategy = createLoadBalanceStrategy(serviceDefinition);
            
            // 创建DISCOVERY路由目标
            DiscoveryRouteService routeTarget = new DiscoveryRouteService(
                    serviceDefinition,
                    protocol,
                    serviceRegistry,
                    loadBalanceStrategy,
                    serviceDefinition.getConfig()
            );
            
            log.info("成功创建DISCOVERY路由目标: {} ({}), 负载均衡: {}", 
                    serviceDefinition.getName(), serviceDefinition.getId(), 
                    loadBalanceStrategy.getStrategyName());
            
            return routeTarget;
            
        } catch (Exception e) {
            log.error("创建DISCOVERY路由目标失败: {}", serviceDefinition.getId(), e);
            throw new IllegalArgumentException("创建DISCOVERY路由目标失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("开始验证DISCOVERY路由目标定义: {}", serviceDefinition.getId());
        
        // 基础字段验证
        if (serviceDefinition.getType() != ServiceType.DISCOVERY) {
            throw new IllegalArgumentException("服务类型必须是DISCOVERY");
        }
        
        if (serviceDefinition.getId() == null || serviceDefinition.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceId");
        }
        
        if (serviceDefinition.getName() == null || serviceDefinition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceName");
        }
        
        if (serviceDefinition.getSupportProtocol() == null || serviceDefinition.getSupportProtocol().getType() == null || serviceDefinition.getSupportProtocol().getType().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定supportProtocol");
        }
        
        // 协议验证
        try {
            serviceDefinition.getSupportProtocol().toProtocol();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的协议类型: " + serviceDefinition.getSupportProtocol().getType(), e);
        }
        
        // 负载均衡策略验证
        validateLoadBalanceStrategy(serviceDefinition);
        
        // 服务发现配置验证
        validateDiscoveryConfig(serviceDefinition);
        
        log.debug("DISCOVERY路由目标定义验证通过: {}", serviceDefinition.getId());
    }
    
    /**
     * 验证负载均衡策略配置
     */
    private void validateLoadBalanceStrategy(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        if (config == null) {
            return; // 使用默认配置
        }
        
        Object strategy = config.get("load-balance-strategy");
        if (strategy != null) {
            String strategyName = strategy.toString();
            if (!isSupportedStrategy(strategyName)) {
                throw new IllegalArgumentException("不支持的负载均衡策略: " + strategyName);
            }
        }
        
        // 验证权重配置（如果是加权策略）
        if ("WEIGHTED_ROUND_ROBIN".equals(strategy)) {
            validateWeightedConfig(serviceDefinition);
        }
    }
    
    /**
     * 验证加权配置
     */
    private void validateWeightedConfig(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        Object weights = config.get("weights");
        
        if (weights instanceof Map<?, ?>) {
            Map<?, ?> weightMap = (Map<?, ?>) weights;
            for (Object weight : weightMap.values()) {
                if (weight instanceof Number) {
                    int w = ((Number) weight).intValue();
                    if (w <= 0) {
                        throw new IllegalArgumentException("权重值必须大于0");
                    }
                } else {
                    throw new IllegalArgumentException("权重值必须是数字类型");
                }
            }
        }
    }
    
    /**
     * 验证服务发现配置
     */
    private void validateDiscoveryConfig(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        if (config == null) {
            return;
        }
        
        // 验证缓存过期时间
        Object cacheExpireTime = config.get("cache-expire-time");
        if (cacheExpireTime != null) {
            try {
                int seconds = Integer.parseInt(cacheExpireTime.toString());
                if (seconds < 5) {
                    throw new IllegalArgumentException("缓存过期时间不能少于5秒");
                }
                if (seconds > 3600) {
                    throw new IllegalArgumentException("缓存过期时间不能超过3600秒");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("缓存过期时间必须是数字: " + cacheExpireTime);
            }
        }
        
        // 验证健康检查配置
        validateHealthCheckConfig(config);
        
        // 验证服务发现特定配置
        validateServiceDiscoveryConfig(config);
    }
    
    /**
     * 验证健康检查配置
     */
    private void validateHealthCheckConfig(Map<String, Object> config) {
        Object healthCheck = config.get("health-check");
        if (healthCheck instanceof Map<?, ?>) {
            Map<?, ?> healthConfig = (Map<?, ?>) healthCheck;
            // 验证健康检查间隔
            Object interval = healthConfig.get("interval");
            if (interval != null) {
                try {
                    int seconds = Integer.parseInt(interval.toString());
                    if (seconds < 5 || seconds > 300) {
                        throw new IllegalArgumentException("健康检查间隔必须在5-300秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查间隔必须是数字: " + interval);
                }
            }
            
            // 验证超时时间
            Object timeout = healthConfig.get("timeout");
            if (timeout != null) {
                try {
                    int seconds = Integer.parseInt(timeout.toString());
                    if (seconds < 1 || seconds > 60) {
                        throw new IllegalArgumentException("健康检查超时时间必须在1-60秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查超时时间必须是数字: " + timeout);
                }
            }
        }
    }
    
    /**
     * 验证服务发现特定配置
     */
    private void validateServiceDiscoveryConfig(Map<String, Object> config) {
        // 验证服务发现标签过滤
        Object tags = config.get("tags");
        if (tags instanceof Map) {
            Map<?, ?> tagMap = (Map<?, ?>) tags;
            for (Object key : tagMap.keySet()) {
                if (!(key instanceof String)) {
                    throw new IllegalArgumentException("服务标签的key必须是字符串类型");
                }
            }
        }
        
        // 验证服务版本过滤
        Object version = config.get("version");
        if (version != null && !(version instanceof String)) {
            throw new IllegalArgumentException("服务版本必须是字符串类型");
        }
        
        // 验证命名空间
        Object namespace = config.get("namespace");
        if (namespace != null && !(namespace instanceof String)) {
            throw new IllegalArgumentException("命名空间必须是字符串类型");
        }
    }
    
    /**
     * 创建负载均衡策略
     */
    private LoadBalanceStrategy createLoadBalanceStrategy(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        
        // 获取策略名称，默认为轮询
        String strategyName = "ROUND_ROBIN";
        if (config != null && config.get("load-balance-strategy") != null) {
            strategyName = config.get("load-balance-strategy").toString();
        }
        
        try {
            LoadBalanceStrategy strategy = createStrategyByName(strategyName, config);
            log.debug("为DISCOVERY路由目标 {} 创建负载均衡策略: {}", serviceDefinition.getId(), strategyName);
            return strategy;
            
        } catch (Exception e) {
            log.error("创建负载均衡策略失败: {}", strategyName, e);
            throw new IllegalArgumentException("创建负载均衡策略失败: " + strategyName, e);
        }
    }
    
    /**
     * 根据策略名称创建策略实例
     */
    private LoadBalanceStrategy createStrategyByName(String strategyName, Map<String, Object> config) {
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
    
    /**
     * 检查是否支持指定的负载均衡策略
     */
    private boolean isSupportedStrategy(String strategyName) {
        String[] supportedStrategies = {"ROUND_ROBIN", "RANDOM", "WEIGHTED_ROUND_ROBIN", "LEAST_CONNECTIONS"};
        for (String supported : supportedStrategies) {
            if (supported.equalsIgnoreCase(strategyName)) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * 获取支持的协议列表
     */
    public String[] getSupportedProtocols() {
        return new String[]{"HTTP", "HTTPS", "WS", "WSS", "TCP", "UDP"};
    }
    
    /**
     * 获取支持的负载均衡策略
     */
    public String[] getSupportedLoadBalanceStrategies() {
        return new String[]{"ROUND_ROBIN", "RANDOM", "WEIGHTED_ROUND_ROBIN", "LEAST_CONNECTIONS"};
    }
    
    /**
     * 获取工厂状态信息
     */
    public String getFactoryInfo() {
        return String.format(
            "DiscoveryRouteTargetFactory{serviceDiscovery=%s, supportedProtocols=[%s], supportedStrategies=[%s]}",
            serviceRegistry.getClass().getSimpleName(),
            String.join(",", getSupportedProtocols()),
            String.join(",", getSupportedLoadBalanceStrategies())
        );
    }
    
    @Override
    public String toString() {
        return getFactoryInfo();
    }
} 