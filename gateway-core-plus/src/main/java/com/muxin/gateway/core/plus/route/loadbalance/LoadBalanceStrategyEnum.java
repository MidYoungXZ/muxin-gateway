package com.muxin.gateway.core.plus.route.loadbalance;

/**
 * 负载均衡策略枚举
 * 定义网关支持的负载均衡策略类型
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum LoadBalanceStrategy {
    
    /**
     * 轮询策略
     */
    ROUND_ROBIN("ROUND_ROBIN", "轮询策略"),
    
    /**
     * 加权轮询策略
     */
    WEIGHTED_ROUND_ROBIN("WEIGHTED_ROUND_ROBIN", "加权轮询策略"),
    
    /**
     * 最少连接策略
     */
    LEAST_CONNECTIONS("LEAST_CONNECTIONS", "最少连接策略"),
    
    /**
     * 一致性哈希策略
     */
    CONSISTENT_HASH("CONSISTENT_HASH", "一致性哈希策略"),
    
    /**
     * 随机策略
     */
    RANDOM("RANDOM", "随机策略");
    
    private final String code;
    private final String description;
    
    LoadBalanceStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取策略代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取策略描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取策略
     */
    public static LoadBalanceStrategy fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (LoadBalanceStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        
        throw new IllegalArgumentException("不支持的负载均衡策略: " + code);
    }
    
    /**
     * 是否为加权策略
     */
    public boolean isWeighted() {
        return this == WEIGHTED_ROUND_ROBIN;
    }
    
    /**
     * 是否为哈希策略
     */
    public boolean isHash() {
        return this == CONSISTENT_HASH;
    }
    
    /**
     * 是否为连接数策略
     */
    public boolean isConnectionBased() {
        return this == LEAST_CONNECTIONS;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}