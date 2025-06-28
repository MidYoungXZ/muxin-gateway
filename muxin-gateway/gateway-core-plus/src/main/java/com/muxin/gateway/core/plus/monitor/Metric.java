package com.muxin.gateway.core.plus.monitor;

import java.util.Map;

/**
 * 基础指标接口
 * 
 * @author muxin
 */
public interface Metric {
    
    /**
     * 获取指标名称
     * 
     * @return 指标名称
     */
    String getName();
    
    /**
     * 获取指标类型
     * 
     * @return 指标类型
     */
    MetricType getType();
    
    /**
     * 获取指标值
     * 
     * @return 指标值
     */
    Object getValue();
    
    /**
     * 获取指标标签
     * 
     * @return 标签映射
     */
    Map<String, String> getTags();
    
    /**
     * 获取指标描述
     * 
     * @return 指标描述
     */
    String getDescription();
    
    /**
     * 获取最后更新时间戳
     * 
     * @return 时间戳（毫秒）
     */
    long getLastUpdateTime();
}