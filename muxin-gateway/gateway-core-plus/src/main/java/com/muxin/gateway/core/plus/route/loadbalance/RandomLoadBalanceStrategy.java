package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 随机负载均衡策略
 * 随机选择可用地址
 *
 * @author muxin
 */
@Slf4j
public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "RANDOM";
    private static final String DESCRIPTION = "随机负载均衡，随机选择可用地址";
    
    private final LoadBalanceStats stats;
    
    public RandomLoadBalanceStrategy() {
        this.stats = LoadBalanceStats.builder()
                .totalSelections(new AtomicLong(0))
                .addressSelections(new ConcurrentHashMap<>())
                .startTime(System.currentTimeMillis())
                .totalSelectionTime(new AtomicLong(0))
                .strategyName(STRATEGY_NAME)
                .build();
    }
    
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        
        long startTime = System.nanoTime();
        
        try {
            // 随机选择
            int index = ThreadLocalRandom.current().nextInt(addresses.size());
            EndpointAddress selected = addresses.get(index);
            
            log.debug("随机选择地址: {} (索引: {}/{})", selected.toUri(), index, addresses.size());
            return selected;
            
        } finally {
            // 记录统计信息
            long selectionTime = System.nanoTime() - startTime;
            int index = ThreadLocalRandom.current().nextInt(addresses.size());
            EndpointAddress selected = addresses.get(index);
            stats.recordSelection(selected.toUri(), selectionTime);
        }
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
        return false; // 无状态策略
    }
    
    @Override
    public void reset() {
        stats.reset();
        log.info("随机策略统计信息已重置");
    }
    
    @Override
    public LoadBalanceStats getStats() {
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("RandomLoadBalanceStrategy{stats=%s}", stats);
    }
} 