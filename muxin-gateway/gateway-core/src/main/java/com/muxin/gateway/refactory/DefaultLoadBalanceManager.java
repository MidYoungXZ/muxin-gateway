package com.muxin.gateway.refactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认负载均衡管理器实现
 * 
 * @author muxin
 */
public class DefaultLoadBalanceManager implements LoadBalanceManager {
    
    private final Map<String, LoadBalanceStrategy> strategies;
    private volatile String defaultStrategyName;
    
    public DefaultLoadBalanceManager() {
        this.strategies = new ConcurrentHashMap<>();
        this.defaultStrategyName = "ROUND_ROBIN";
        
        // 注册默认策略
        registerStrategy("ROUND_ROBIN", new RoundRobinLoadBalancer());
    }
    
    @Override
    public void registerStrategy(String name, LoadBalanceStrategy strategy) {
        if (name != null && strategy != null) {
            strategies.put(name, strategy);
            System.out.println("[LOAD_BALANCE_MANAGER] 注册负载均衡策略: " + name);
        }
    }
    
    @Override
    public LoadBalanceStrategy getStrategy(String name) {
        return strategies.get(name);
    }
    
    @Override
    public EndpointAddress selectTarget(String serviceName, List<EndpointAddress> availableTargets, 
                                      UniversalRequestContext context) {
        if (availableTargets == null || availableTargets.isEmpty()) {
            return null;
        }
        
        // 如果只有一个地址，直接返回
        if (availableTargets.size() == 1) {
            return availableTargets.get(0);
        }
        
        // 获取负载均衡策略，优先从上下文中获取
        String strategyName = context.getAttribute("load-balance-strategy", String.class);
        if (strategyName == null) {
            strategyName = defaultStrategyName;
        }
        
        LoadBalanceStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            // 如果没有可用策略，使用默认策略
            strategy = strategies.get(defaultStrategyName);
        }
        
        if (strategy == null) {
            // 如果还没有策略，返回第一个地址
            System.err.println("[LOAD_BALANCE_MANAGER] 没有可用的负载均衡策略，使用第一个地址");
            return availableTargets.get(0);
        }
        
        try {
            EndpointAddress selected = strategy.select(availableTargets, context);
            System.out.println(String.format("[LOAD_BALANCE_MANAGER] 为服务 %s 选择端点: %s 使用策略: %s", 
                serviceName, selected != null ? selected.toUri() : "null", strategy.getName()));
            return selected;
        } catch (Exception e) {
            System.err.println("负载均衡选择失败: " + e.getMessage());
            return availableTargets.get(0);
        }
    }
    
    /**
     * 移除策略
     */
    public void removeStrategy(String name) {
        if (name != null) {
            LoadBalanceStrategy removed = strategies.remove(name);
            if (removed != null) {
                System.out.println("[LOAD_BALANCE_MANAGER] 移除负载均衡策略: " + name);
            }
        }
    }
    
    /**
     * 设置默认策略
     */
    public void setDefaultStrategy(String strategyName) {
        if (strategies.containsKey(strategyName)) {
            this.defaultStrategyName = strategyName;
            System.out.println("[LOAD_BALANCE_MANAGER] 设置默认策略: " + strategyName);
        } else {
            throw new IllegalArgumentException("策略不存在: " + strategyName);
        }
    }
    
    /**
     * 获取所有策略名称
     */
    public java.util.Set<String> getStrategyNames() {
        return new java.util.HashSet<>(strategies.keySet());
    }
    
    /**
     * 获取策略统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalStrategies", strategies.size());
        stats.put("defaultStrategy", defaultStrategyName);
        stats.put("availableStrategies", getStrategyNames());
        return stats;
    }
} 