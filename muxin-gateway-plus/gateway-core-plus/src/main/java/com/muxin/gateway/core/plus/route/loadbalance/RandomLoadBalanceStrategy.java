package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡策略
 * 随机选择可用地址
 *
 * @author muxin
 */
@Slf4j
public class RandomLoadBalanceStrategy extends LoadBalanceStrategy {
    
    private static final String STRATEGY_NAME = "RANDOM";
    private static final String DESCRIPTION = "随机负载均衡，随机选择可用地址";
    
    /**
     * 构造函数
     * @param definition 负载均衡定义
     */
    public RandomLoadBalanceStrategy(LoadBalanceDefinition definition) {
        super(definition);
        log.debug("创建随机负载均衡策略，策略配置: {}", definition.getStrategy());
    }
    
    @Override
    public EndpointAddress select(List<EndpointAddress> addresses, RequestContext context) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        
        // 随机选择
        int index = ThreadLocalRandom.current().nextInt(addresses.size());
        EndpointAddress selected = addresses.get(index);
        
        log.debug("随机选择地址: {} (索引: {}/{})", selected.toUri(), index, addresses.size());
        return selected;
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
        return false; // 无状态策略
    }
    
    @Override
    public void reset() {
        log.debug("随机策略重置（无状态，无操作）");
    }
    
    @Override
    public String toString() {
        return String.format("RandomLoadBalanceStrategy{strategy='%s'}", getStrategyName());
    }
} 