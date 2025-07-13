package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询负载均衡策略
 * 依次轮询选择可用地址
 *
 * @author muxin
 */
@Slf4j
public class RoundRobinLoadBalanceStrategy implements LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "ROUND_ROBIN";
    private static final String DESCRIPTION = "轮询负载均衡，依次选择可用地址";
    
    private final AtomicInteger counter = new AtomicInteger(0);
    private final LoadBalanceStats stats;
    
    public RoundRobinLoadBalanceStrategy() {
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
            // 轮询选择
            int index = getNextIndex(addresses.size());
            EndpointAddress selected = addresses.get(index);
            
            log.debug("轮询选择地址: {} (索引: {}/{})", selected.toUri(), index, addresses.size());
            return selected;
            
        } finally {
            // 记录统计信息
            long selectionTime = System.nanoTime() - startTime;
            EndpointAddress selected = addresses.get(getNextIndex(addresses.size()) - 1);
            stats.recordSelection(selected.toUri(), selectionTime);
        }
    }
    
    /**
     * 获取下一个索引
     */
    private int getNextIndex(int size) {
        return counter.getAndIncrement() % size;
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
        return true; // 有计数器状态
    }
    
    @Override
    public void reset() {
        counter.set(0);
        stats.reset();
        log.info("轮询策略状态已重置");
    }
    
    @Override
    public LoadBalanceStats getStats() {
        return stats;
    }
    
    /**
     * 获取当前计数器值
     */
    public int getCurrentCounter() {
        return counter.get();
    }
    
    @Override
    public String toString() {
        return String.format("RoundRobinLoadBalanceStrategy{counter=%d, stats=%s}", 
                counter.get(), stats);
    }
} 