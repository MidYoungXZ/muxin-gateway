package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 加权轮询负载均衡策略
 * 根据地址权重进行轮询选择
 *
 * @author muxin
 */
@Slf4j
public class WeightedRoundRobinLoadBalanceStrategy extends LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "WEIGHTED_ROUND_ROBIN";
    private static final String DESCRIPTION = "加权轮询负载均衡，根据权重选择地址";
    
    private final ConcurrentHashMap<String, WeightedNode> weightedNodes = new ConcurrentHashMap<>();
    private final boolean smoothWeighted;
    
    /**
     * 构造函数
     * @param definition 负载均衡定义
     */
    public WeightedRoundRobinLoadBalanceStrategy(LoadBalanceDefinition definition) {
        super(definition);
        // 从配置中获取平滑加权配置，默认为true
        this.smoothWeighted = getConfigValue("smooth-weighted", true);
        log.debug("创建加权轮询负载均衡策略，策略配置: {}, 平滑加权: {}", 
                definition.getStrategy(), smoothWeighted);
    }
    
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        
        // 更新权重节点
        updateWeightedNodes(addresses);
        
        // 平滑加权轮询算法
        EndpointAddress selected = selectByWeight(addresses);
        
        log.debug("加权轮询选择地址: {} (权重: {})", 
                selected.toUri(), getWeight(selected));
        return selected;
    }
    
    /**
     * 更新权重节点信息
     */
    private void updateWeightedNodes(List<EndpointAddress> addresses) {
        for (EndpointAddress address : addresses) {
            String key = address.toUri();
            
            // 如果节点不存在，则创建新节点
            if (!weightedNodes.containsKey(key)) {
                int weight = getWeight(address);
                WeightedNode node = new WeightedNode(address, weight, 0);
                weightedNodes.put(key, node);
                
                log.debug("添加权重节点: {} (权重: {})", key, weight);
            }
        }
        
        // 清理已删除的地址对应的节点
        weightedNodes.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            boolean exists = addresses.stream()
                    .anyMatch(addr -> addr.toUri().equals(key));
            
            if (!exists) {
                log.debug("移除权重节点: {}", key);
            }
            
            return !exists;
        });
    }
    
    /**
     * 根据权重选择地址（平滑加权轮询算法）
     */
    private EndpointAddress selectByWeight(List<EndpointAddress> addresses) {
        synchronized (this) {
            int totalWeight = 0;
            WeightedNode maxWeightNode = null;
            
            // 找到当前权重最大的节点，并计算总权重
            for (EndpointAddress address : addresses) {
                WeightedNode node = weightedNodes.get(address.toUri());
                if (node != null) {
                    // 增加当前权重
                    node.increaseCurrent();
                    totalWeight += node.getWeight();
                    
                    // 找到当前权重最大的节点
                    if (maxWeightNode == null || node.getCurrentWeight() > maxWeightNode.getCurrentWeight()) {
                        maxWeightNode = node;
                    }
                }
            }
            
            if (maxWeightNode == null) {
                // 降级处理：如果没有权重信息，使用第一个地址
                log.warn("未找到权重节点，使用第一个地址");
                return addresses.get(0);
            }
            
            // 减少选中节点的当前权重
            maxWeightNode.decreaseCurrent(totalWeight);
            
            return maxWeightNode.getAddress();
        }
    }
    
    /**
     * 获取地址权重（从协议特定信息中获取）
     */
    private int getWeight(EndpointAddress address) {
        try {
            Map<String, Object> protocolInfo = address.getProtocolSpecificInfo();
            if (protocolInfo != null && protocolInfo.containsKey("weight")) {
                Object weightObj = protocolInfo.get("weight");
                if (weightObj instanceof Number) {
                    return Math.max(1, ((Number) weightObj).intValue());
                }
            }
        } catch (Exception e) {
            log.warn("获取地址权重失败: {}", address.toUri(), e);
        }
        
        // 默认权重为1
        return 1;
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    @Override
    public boolean requiresWeight() {
        return true;
    }
    
    @Override
    public boolean isStateful() {
        return true; // 有权重状态
    }
    
    @Override
    public void reset() {
        synchronized (this) {
            weightedNodes.clear();
            log.debug("加权轮询策略状态已重置");
        }
    }
    
    /**
     * 权重节点内部类
     */
    private static class WeightedNode {
        private final EndpointAddress address;
        private final int weight;
        private int currentWeight;
        
        public WeightedNode(EndpointAddress address, int weight, int currentWeight) {
            this.address = address;
            this.weight = weight;
            this.currentWeight = currentWeight;
        }
        
        public EndpointAddress getAddress() {
            return address;
        }
        
        public int getWeight() {
            return weight;
        }
        
        public int getCurrentWeight() {
            return currentWeight;
        }
        
        public void increaseCurrent() {
            this.currentWeight += weight;
        }
        
        public void decreaseCurrent(int totalWeight) {
            this.currentWeight -= totalWeight;
        }
        
        @Override
        public String toString() {
            return String.format("WeightedNode{address=%s, weight=%d, currentWeight=%d}", 
                    address.toUri(), weight, currentWeight);
        }
    }
    
    @Override
    public String toString() {
        return String.format("WeightedRoundRobinLoadBalanceStrategy{strategy='%s', nodes=%d, smoothWeighted=%s}", 
                getStrategyName(), weightedNodes.size(), smoothWeighted);
    }
} 