package com.muxin.gateway.core.plus.route.loadbalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 负载均衡配置类
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadBalanceDefinition {
    
    /**
     * 负载均衡策略名称
     */
    @Builder.Default
    private String strategy = "ROUND_ROBIN";
    
    /**
     * 策略配置参数
     */
    private Map<String, Object> config;
    
    /**
     * 权重来源（仅用于服务发现）
     * REGISTRY: 来自注册中心
     * EQUAL: 平均分配
     */
    private String weightSource;
    
    /**
     * 权重元数据键名（仅用于服务发现）
     */
    private String weightMetadataKey;
    
    /**
     * 哈希键名（用于一致性哈希策略）
     */
    private String hashKey;
    
    /**
     * 获取配置参数
     */
    public Object getConfigValue(String key) {
        return config != null ? config.get(key) : null;
    }
    
    /**
     * 获取配置参数（带默认值）
     */
    public <T> T getConfigValue(String key, T defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        
        @SuppressWarnings("unchecked")
        T value = (T) config.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 设置配置参数
     */
    public void setConfigValue(String key, Object value) {
        if (config == null) {
            config = new java.util.HashMap<>();
        }
        config.put(key, value);
    }
    
    /**
     * 是否为加权策略
     */
    public boolean isWeightedStrategy() {
        return "WEIGHTED_ROUND_ROBIN".equalsIgnoreCase(strategy) ||
               "WEIGHTED_RANDOM".equalsIgnoreCase(strategy);
    }
    
    /**
     * 是否为哈希策略
     */
    public boolean isHashStrategy() {
        return "CONSISTENT_HASH".equalsIgnoreCase(strategy);
    }
} 