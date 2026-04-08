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
     * 过滤器名称
     */
    private String name;
    
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
     * 过滤器参数
     */
    private Map<String, Object> args;
    
    /**
     * 获取参数
     */
    public Object getArg(String key) {
        return args != null ? args.get(key) : null;
    }
    
    /**
     * 获取参数（带默认值）
     */
    public <T> T getArg(String key, T defaultValue) {
        if (args == null) {
            return defaultValue;
        }
        
        @SuppressWarnings("unchecked")
        T value = (T) args.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取字符串参数
     */
    public String getStringArg(String key) {
        Object value = getArg(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取字符串参数（带默认值）
     */
    public String getStringArg(String key, String defaultValue) {
        String value = getStringArg(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 设置参数
     */
    public void setArg(String key, Object value) {
        if (args == null) {
            args = new java.util.HashMap<>();
        }
        args.put(key, value);
    }

    /**
     * 获取布尔参数
     */
    public boolean getBooleanArg(String key, boolean defaultValue) {
        Object value = getArg(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * 获取整数参数
     */
    public int getIntArg(String key, int defaultValue) {
        Object value = getArg(key);
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

    public long getLongArg(String key, long defaultValue) {
        Object value = getArg(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}