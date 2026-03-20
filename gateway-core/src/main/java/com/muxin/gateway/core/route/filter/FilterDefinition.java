package com.muxin.gateway.core.route.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 过滤器配置类
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDefinition {
    
    /**
     * 过滤器类型
     */
    private String type;
    
    /**
     * 执行顺序
     */
    @Builder.Default
    private int order = 0;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;
    
    /**
     * 过滤器配置参数
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

    /**
     * 获取布尔配置参数
     */
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        Object value = getConfigValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * 获取整数配置参数
     */
    public int getIntConfig(String key, int defaultValue) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
} 