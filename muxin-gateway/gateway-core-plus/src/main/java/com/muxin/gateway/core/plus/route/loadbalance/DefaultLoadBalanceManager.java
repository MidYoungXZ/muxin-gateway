package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认负载均衡管理器实现
 * 
 * @author muxin
 */
@Slf4j
public class DefaultLoadBalanceManager implements LoadBalanceManager {
    
    private final Map<String, LoadBalanceStrategy> strategies;
    private volatile String defaultStrategyName;
    
    public DefaultLoadBalanceManager() {
        this.strategies = new ConcurrentHashMap<>();
        this.defaultStrategyName = "ROUND_ROBIN";
        
        // 注册默认策略
        save(new RoundRobinLoadBalancer());
    }
    
    // Repository 接口实现
    @Override
    public LoadBalanceStrategy save(LoadBalanceStrategy entity) {
        if (entity != null && entity.getName() != null) {
            strategies.put(entity.getName(), entity);
            log.info("注册负载均衡策略: {}", entity.getName());
            return entity;
        }
        return null;
    }
    
    @Override
    public void removeByUniqueCode(String name) {
        if (name != null) {
            LoadBalanceStrategy removed = strategies.remove(name);
            if (removed != null) {
                log.info("移除负载均衡策略: {}", name);
            }
        }
    }
    
    @Override
    public LoadBalanceStrategy findByUniqueCode(String name) {
        return strategies.get(name);
    }
    
    @Override
    public Collection<LoadBalanceStrategy> findAll() {
        return strategies.values();
    }
    
    // 业务特定方法
    @Override
    public EndpointAddress selectTarget(String serviceName, List<EndpointAddress> availableTargets,
                                        RequestContext context) {
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
            log.warn("没有可用的负载均衡策略，使用第一个地址");
            return availableTargets.get(0);
        }
        
        try {
            EndpointAddress selected = strategy.select(availableTargets, context);
            log.debug("为服务 {} 选择端点: {} 使用策略: {}", 
                serviceName, selected != null ? selected.toUri() : "null", strategy.getName());
            return selected;
        } catch (Exception e) {
            log.error("负载均衡选择失败: {}", e.getMessage());
            return availableTargets.get(0);
        }
    }
    
    /**
     * 设置默认策略
     */
    public void setDefaultStrategy(String strategyName) {
        if (strategies.containsKey(strategyName)) {
            this.defaultStrategyName = strategyName;
            log.info("设置默认负载均衡策略: {}", strategyName);
        }
    }
    
    /**
     * 获取默认策略名称
     */
    public String getDefaultStrategyName() {
        return defaultStrategyName;
    }
    
    /**
     * 获取策略统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalStrategies", strategies.size());
        stats.put("defaultStrategy", defaultStrategyName);
        stats.put("availableStrategies", strategies.keySet());
        return stats;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}