package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最少连接负载均衡策略
 * 选择当前连接数最少的地址
 *
 * @author muxin
 */
@Slf4j
public class LeastConnectionsLoadBalanceStrategy extends LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "LEAST_CONNECTIONS";
    private static final String DESCRIPTION = "最少连接负载均衡，选择连接数最少的地址";
    
    private final ConcurrentHashMap<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * @param definition 负载均衡定义
     */
    public LeastConnectionsLoadBalanceStrategy(LoadBalanceDefinition definition) {
        super(definition);
        log.debug("创建最少连接负载均衡策略，策略配置: {}", definition.getStrategy());
    }
    
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        
        // 选择连接数最少的地址
        EndpointAddress selected = selectLeastConnections(addresses);
        
        // 增加连接计数
        incrementConnectionCount(selected);
        
        log.debug("最少连接选择地址: {} (当前连接数: {})", 
                selected.toUri(), getConnectionCount(selected));
        return selected;
    }
    
    /**
     * 选择连接数最少的地址
     */
    private EndpointAddress selectLeastConnections(List<EndpointAddress> addresses) {
        EndpointAddress selected = addresses.get(0);
        int minConnections = getConnectionCount(selected);
        
        for (EndpointAddress address : addresses) {
            int currentConnections = getConnectionCount(address);
            if (currentConnections < minConnections) {
                minConnections = currentConnections;
                selected = address;
            }
        }
        
        return selected;
    }
    
    /**
     * 增加连接计数
     */
    private void incrementConnectionCount(EndpointAddress address) {
        String key = address.toUri();
        connectionCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 减少连接计数（当连接关闭时调用）
     */
    public void decrementConnectionCount(EndpointAddress address) {
        String key = address.toUri();
        AtomicInteger count = connectionCounts.get(key);
        if (count != null) {
            int newCount = count.decrementAndGet();
            if (newCount <= 0) {
                // 清理计数为0或负数的条目
                connectionCounts.remove(key);
            }
        }
    }
    
    /**
     * 获取连接计数
     */
    private int getConnectionCount(EndpointAddress address) {
        String key = address.toUri();
        AtomicInteger count = connectionCounts.get(key);
        return count != null ? count.get() : 0;
    }
    
    /**
     * 获取所有连接计数
     */
    public Map<String, Integer> getAllConnectionCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        connectionCounts.forEach((key, value) -> result.put(key, value.get()));
        return result;
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
    public boolean isStateful() {
        return true; // 有连接计数状态
    }
    
    @Override
    public void reset() {
        connectionCounts.clear();
        log.debug("最少连接策略状态已重置");
    }
    
    /**
     * 清理无效的连接计数
     */
    public void cleanupInvalidConnections(List<EndpointAddress> validAddresses) {
        if (validAddresses == null || validAddresses.isEmpty()) {
            connectionCounts.clear();
            return;
        }
        
        // 获取所有有效地址的URI
        java.util.Set<String> validUris = new java.util.HashSet<>();
        validAddresses.forEach(addr -> validUris.add(addr.toUri()));
        
        // 移除无效的连接计数
        connectionCounts.entrySet().removeIf(entry -> {
            boolean isInvalid = !validUris.contains(entry.getKey());
            if (isInvalid) {
                log.debug("清理无效连接计数: {}", entry.getKey());
            }
            return isInvalid;
        });
    }
    
    @Override
    public String toString() {
        return String.format("LeastConnectionsLoadBalanceStrategy{strategy='%s', trackedConnections=%d}", 
                getStrategyName(), connectionCounts.size());
    }
} 