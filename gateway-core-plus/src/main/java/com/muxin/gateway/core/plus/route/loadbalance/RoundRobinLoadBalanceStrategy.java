package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 * 依次轮询选择可用地址
 *
 * @author muxin
 */
@Slf4j
public class RoundRobinLoadBalanceStrategy extends LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "ROUND_ROBIN";
    private static final String DESCRIPTION = "轮询负载均衡，依次选择可用地址";
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
    /**
     * 构造函数
     * @param definition 负载均衡定义
     */
    public RoundRobinLoadBalanceStrategy(LoadBalanceDefinition definition) {
        super(definition);
        log.debug("创建轮询负载均衡策略，策略配置: {}", definition.getStrategy());
    }
    
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        
        // 轮询选择
        int index = getNextIndex(addresses.size());
        EndpointAddress selected = addresses.get(index);
        
        log.debug("轮询选择地址: {} (索引: {}/{})", selected.toUri(), index, addresses.size());
        return selected;
    }
    
    /**
     * 获取下一个索引
     */
    private int getNextIndex(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("地址数量必须大于0");
        }
        
        // 使用原子操作确保线程安全
        return Math.abs(counter.getAndIncrement()) % size;
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
        return true; // 有计数器状态
    }
    
    @Override
    public void reset() {
        counter.set(0);
        log.debug("重置轮询负载均衡计数器");
    }
    
    /**
     * 获取当前计数器值
     */
    public int getCurrentCounter() {
        return counter.get();
    }
    
    @Override
    public String toString() {
        return String.format("RoundRobinLoadBalanceStrategy{strategy='%s', counter=%d}", 
                getStrategyName(), counter.get());
    }
} 