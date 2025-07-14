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
public class LeastConnectionsLoadBalanceStrategy implements LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "LEAST_CONNECTIONS";
    private static final String DESCRIPTION = "最少连接负载均衡，选择连接数最少的地址";
    
    private final ConcurrentHashMap<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    public LeastConnectionsLoadBalanceStrategy() {
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
            int connections = getConnectionCount(address);
            if (connections < minConnections) {
                minConnections = connections;
                selected = address;
            }
        }
        
        return selected;
    }
    
    /**
     * 获取地址的连接数
     */
    private int getConnectionCount(EndpointAddress address) {
        return connectionCounts.computeIfAbsent(address.toUri(), k -> new AtomicInteger(0)).get();
    }
    
    /**
     * 增加连接计数
     */
    private void incrementConnectionCount(EndpointAddress address) {
        connectionCounts.computeIfAbsent(address.toUri(), k -> new AtomicInteger(0))
                .incrementAndGet();
    }
    
    /**
     * 减少连接计数（当连接关闭时调用）
     */
    public void decrementConnectionCount(EndpointAddress address) {
        AtomicInteger count = connectionCounts.get(address.toUri());
        if (count != null) {
            int newCount = count.decrementAndGet();
            if (newCount < 0) {
                count.set(0); // 确保不会小于0
            }
            log.debug("减少连接计数: {} (当前连接数: {})", address.toUri(), count.get());
        }
    }
    
    /**
     * 设置地址的连接数（用于外部连接池同步）
     */
    public void setConnectionCount(EndpointAddress address, int count) {
        connectionCounts.computeIfAbsent(address.toUri(), k -> new AtomicInteger(0))
                .set(Math.max(0, count));
        log.debug("设置连接计数: {} = {}", address.toUri(), count);
    }
    
    /**
     * 获取所有地址的连接数统计
     */
    public Map<String, Integer> getAllConnectionCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        connectionCounts.forEach((address, count) -> result.put(address, count.get()));
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
    public boolean requiresWeight() {
        return false;
    }
    
    @Override
    public boolean isStateful() {
        return true; // 有连接计数状态
    }
    
    @Override
    public void reset() {
        connectionCounts.clear();
        log.info("最少连接策略状态已重置");
    }
    
    @Override
    public String toString() {
        return String.format("LeastConnectionsLoadBalanceStrategy{addresses=%d, totalConnections=%d}", 
                connectionCounts.size(),
                connectionCounts.values().stream().mapToInt(AtomicInteger::get).sum());
    }
} 