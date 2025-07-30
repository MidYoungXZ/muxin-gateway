package com.muxin.gateway.core.plus.route.loadbalance;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 负载均衡策略工厂
 * 根据负载均衡定义创建相应的策略实例
 * 支持默认配置创建
 *
 * @author muxin
 */
@Slf4j
public class LoadBalanceStrategyFactory {
    
    /**
     * 默认负载均衡策略名称
     */
    private static final String DEFAULT_STRATEGY = "ROUND_ROBIN";
    
    /**
     * 私有构造函数，防止实例化
     */
    private LoadBalanceStrategyFactory() {
    }
    
    /**
     * 创建负载均衡策略
     * @param definition 负载均衡定义（可能为null）
     * @return 负载均衡策略实例
     */
    public static LoadBalanceStrategy createStrategy(LoadBalanceDefinition definition) {
        // 如果用户未配置，使用默认配置
        if (definition == null) {
            definition = createDefaultLoadBalanceDefinition();
            log.debug("未配置负载均衡策略，使用默认配置: {}", DEFAULT_STRATEGY);
        }
        
        String strategyName = definition.getStrategy();
        if (strategyName == null || strategyName.trim().isEmpty()) {
            strategyName = DEFAULT_STRATEGY;
            log.warn("负载均衡策略名称为空，使用默认策略: {}", DEFAULT_STRATEGY);
        }
        
        return createStrategyByName(strategyName.toUpperCase(), definition);
    }
    
    /**
     * 根据策略名称创建策略实例
     */
    private static LoadBalanceStrategy createStrategyByName(String strategyName, LoadBalanceDefinition definition) {
        try {
            switch (strategyName) {
                case "ROUND_ROBIN":
                    return new RoundRobinLoadBalanceStrategy(definition);
                case "RANDOM":
                    return new RandomLoadBalanceStrategy(definition);
                case "WEIGHTED_ROUND_ROBIN":
                    return new WeightedRoundRobinLoadBalanceStrategy(definition);
                case "LEAST_CONNECTIONS":
                    return new LeastConnectionsLoadBalanceStrategy(definition);
                default:
                    log.warn("未知的负载均衡策略: {}, 使用默认策略: {}", strategyName, DEFAULT_STRATEGY);
                    // 创建默认策略，但保持原有的definition配置
                    LoadBalanceDefinition defaultDef = LoadBalanceDefinition.builder()
                            .strategy(DEFAULT_STRATEGY)
                            .config(definition.getConfig())  // 保持用户的其他配置
                            .build();
                    return new RoundRobinLoadBalanceStrategy(defaultDef);
            }
        } catch (Exception e) {
            log.error("创建负载均衡策略失败: {}", strategyName, e);
            // 降级处理：创建默认策略
            LoadBalanceDefinition fallbackDef = createDefaultLoadBalanceDefinition();
            return new RoundRobinLoadBalanceStrategy(fallbackDef);
        }
    }
    
    /**
     * 创建默认负载均衡定义
     */
    public static LoadBalanceDefinition createDefaultLoadBalanceDefinition() {
        return LoadBalanceDefinition.builder()
                .strategy(DEFAULT_STRATEGY)
                .config(createDefaultConfig())
                .build();
    }
    
    /**
     * 创建默认配置
     */
    private static Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("reset-period", 3600);  // 重置周期：1小时
        config.put("connection-timeout", 5000);  // 连接超时：5秒
        return config;
    }
    
    /**
     * 验证负载均衡定义
     * @param definition 负载均衡定义
     * @return true表示有效，false表示无效
     */
    public static boolean validateDefinition(LoadBalanceDefinition definition) {
        if (definition == null) {
            return false;
        }
        
        String strategy = definition.getStrategy();
        if (strategy == null || strategy.trim().isEmpty()) {
            return false;
        }
        
        return isSupportedStrategy(strategy);
    }
    
    /**
     * 检查是否为支持的策略
     */
    public static boolean isSupportedStrategy(String strategyName) {
        if (strategyName == null) {
            return false;
        }
        
        String upperName = strategyName.toUpperCase();
        return "ROUND_ROBIN".equals(upperName) ||
               "RANDOM".equals(upperName) ||
               "WEIGHTED_ROUND_ROBIN".equals(upperName) ||
               "LEAST_CONNECTIONS".equals(upperName);
    }
    
    /**
     * 获取所有支持的策略名称
     */
    public static String[] getSupportedStrategies() {
        return new String[]{
            "ROUND_ROBIN",
            "RANDOM", 
            "WEIGHTED_ROUND_ROBIN",
            "LEAST_CONNECTIONS"
        };
    }
    
    /**
     * 获取策略描述
     */
    public static String getStrategyDescription(String strategyName) {
        if (strategyName == null) {
            return "未知策略";
        }
        
        switch (strategyName.toUpperCase()) {
            case "ROUND_ROBIN":
                return "轮询负载均衡，依次选择可用地址";
            case "RANDOM":
                return "随机负载均衡，随机选择可用地址";
            case "WEIGHTED_ROUND_ROBIN":
                return "加权轮询负载均衡，根据权重选择地址";
            case "LEAST_CONNECTIONS":
                return "最少连接负载均衡，选择连接数最少的地址";
            default:
                return "未知策略: " + strategyName;
        }
    }
    
    /**
     * 创建带有自定义配置的负载均衡定义
     */
    public static LoadBalanceDefinition createDefinition(String strategy, Map<String, Object> config) {
        return LoadBalanceDefinition.builder()
                .strategy(strategy != null ? strategy : DEFAULT_STRATEGY)
                .config(config != null ? config : createDefaultConfig())
                .build();
    }
} 