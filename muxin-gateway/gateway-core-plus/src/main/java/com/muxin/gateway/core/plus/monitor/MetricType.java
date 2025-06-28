package com.muxin.gateway.core.plus.monitor;

/**
 * 指标类型枚举
 * 
 * @author muxin
 */
public enum MetricType {
    
    /**
     * 计数器 - 只能递增的累计指标
     */
    COUNTER("counter"),
    
    /**
     * 计量器 - 可以任意变化的瞬时值指标
     */
    GAUGE("gauge"),
    
    /**
     * 计时器 - 测量时间分布的指标
     */
    TIMER("timer"),
    
    /**
     * 直方图 - 测量数值分布的指标
     */
    HISTOGRAM("histogram");
    
    private final String value;
    
    MetricType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
} 