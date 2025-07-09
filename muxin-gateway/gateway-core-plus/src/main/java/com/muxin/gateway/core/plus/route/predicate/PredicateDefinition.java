package com.muxin.gateway.core.plus.route.predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 断言配置类
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredicateDefinition {
    
    /**
     * 断言类型
     */
    private String type;
    
    /**
     * 断言配置参数
     */
    private Map<String, Object> config;
    
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
     * 获取字符串配置参数
     */
    public String getStringConfig(String key) {
        Object value = getConfigValue(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取字符串配置参数（带默认值）
     */
    public String getStringConfig(String key, String defaultValue) {
        String value = getStringConfig(key);
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
} 