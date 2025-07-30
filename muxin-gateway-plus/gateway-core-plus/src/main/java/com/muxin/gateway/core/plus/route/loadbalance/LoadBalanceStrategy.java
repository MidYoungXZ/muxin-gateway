package com.muxin.gateway.core.plus.route.loadbalance;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.List;
import java.util.Objects;

/**
 * 负载均衡策略抽象类
 * 定义负载均衡算法的基础契约，内部保存负载均衡定义信息
 *
 * @author muxin
 */
public abstract class LoadBalanceStrategy {
    
    /**
     * 负载均衡定义信息
     */
    protected final LoadBalanceDefinition definition;
    
    /**
     * 构造函数
     * @param definition 负载均衡定义，不能为空
     */
    protected LoadBalanceStrategy(LoadBalanceDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "LoadBalanceDefinition不能为空");
    }
    
    /**
     * 获取负载均衡定义
     * @return 负载均衡定义信息
     */
    public LoadBalanceDefinition getDefinition() {
        return definition;
    }
    
    /**
     * 从可用地址列表中选择一个目标地址
     *
     * @param addresses 可用的地址列表
     * @param context   请求上下文
     * @return 选中的目标地址
     */
    public abstract EndpointAddress select(List<EndpointAddress> addresses, RequestContext context);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    public abstract String getStrategyName();
    
    /**
     * 获取策略描述
     *
     * @return 策略描述
     */
    public abstract String getDescription();
    
    /**
     * 策略是否需要权重信息
     *
     * @return true表示需要权重，false表示不需要
     */
    public boolean requiresWeight() {
        return false;
    }
    
    /**
     * 策略是否有状态
     * 有状态的策略在多线程环境下需要考虑线程安全
     *
     * @return true表示有状态，false表示无状态
     */
    public boolean isStateful() {
        return false;
    }
    
    /**
     * 重置策略状态（如果是有状态策略）
     */
    public void reset() {
        // 默认实现：无操作
    }
    
    /**
     * 获取策略配置
     * @return 策略配置Map
     */
    protected java.util.Map<String, Object> getConfig() {
        return definition != null ? definition.getConfig() : java.util.Collections.emptyMap();
    }
    
    /**
     * 获取指定配置项的值
     * @param key 配置项键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfigValue(String key, T defaultValue) {
        java.util.Map<String, Object> config = getConfig();
        Object value = config.get(key);
        if (value != null && defaultValue != null && defaultValue.getClass().isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
} 