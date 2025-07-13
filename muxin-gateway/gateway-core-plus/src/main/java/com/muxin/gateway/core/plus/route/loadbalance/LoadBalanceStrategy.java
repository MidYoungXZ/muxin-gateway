package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.List;

/**
 * 负载均衡策略接口
 * 定义负载均衡算法的基础契约
 *
 * @author muxin
 */
public interface LoadBalanceStrategy {
    
    /**
     * 从可用地址列表中选择一个目标地址
     *
     * @param addresses 可用的地址列表
     * @param context   请求上下文
     * @return 选中的目标地址
     */
    EndpointAddress select(List<EndpointAddress> addresses, RequestContext context);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 获取策略描述
     *
     * @return 策略描述
     */
    String getDescription();
    
    /**
     * 策略是否需要权重信息
     *
     * @return true表示需要权重，false表示不需要
     */
    default boolean requiresWeight() {
        return false;
    }
    
    /**
     * 策略是否有状态
     * 有状态的策略在多线程环境下需要考虑线程安全
     *
     * @return true表示有状态，false表示无状态
     */
    default boolean isStateful() {
        return false;
    }
    
    /**
     * 重置策略状态（如果是有状态策略）
     */
    default void reset() {
        // 默认实现：无操作
    }
    
    /**
     * 获取策略统计信息
     *
     * @return 统计信息，可以为null
     */
    default LoadBalanceStats getStats() {
        return null;
    }
} 