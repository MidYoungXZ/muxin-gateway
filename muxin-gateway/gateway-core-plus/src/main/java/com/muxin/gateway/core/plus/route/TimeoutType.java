package com.muxin.gateway.core.plus.route;

/**
 * 超时类型枚举
 *
 * @author muxin
 */
public enum TimeoutType {
    
    /**
     * 连接超时
     */
    CONNECTION("connection", "连接超时"),
    
    /**
     * 请求超时
     */
    REQUEST("request", "请求超时"),
    
    /**
     * 总超时（包含重试）
     */
    TOTAL("total", "总超时"),
    
    /**
     * 读取超时
     */
    READ("read", "读取超时"),
    
    /**
     * 写入超时
     */
    WRITE("write", "写入超时"),
    
    /**
     * 熔断器超时
     */
    CIRCUIT_BREAKER("circuitBreaker", "熔断器超时");
    
    private final String key;
    private final String description;
    
    TimeoutType(String key, String description) {
        this.key = key;
        this.description = description;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据key获取超时类型
     */
    public static TimeoutType fromKey(String key) {
        for (TimeoutType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的超时类型: " + key);
    }
} 