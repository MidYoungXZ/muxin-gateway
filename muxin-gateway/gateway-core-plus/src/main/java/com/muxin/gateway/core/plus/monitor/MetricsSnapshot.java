package com.muxin.gateway.core.plus.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 指标快照接口
 * 提供系统中所有指标在某个时刻的统一视图
 * 
 * @author muxin
 */
public interface MetricsSnapshot {
    
    /**
     * 获取快照时间戳
     * 
     * @return 时间戳（毫秒）
     */
    long getTimestamp();
    
    /**
     * 获取所有指标
     * 
     * @return 指标集合
     */
    Collection<Metric> getMetrics();
    
    /**
     * 根据名称获取指标
     * 
     * @param name 指标名称
     * @return 指标对象，不存在则返回null
     */
    Metric getMetric(String name);
    
    /**
     * 根据类型获取指标
     * 
     * @param type 指标类型
     * @return 该类型的所有指标
     */
    Collection<Metric> getMetricsByType(MetricType type);
    
    /**
     * 根据标签过滤指标
     * 
     * @param tags 标签映射
     * @return 匹配标签的指标
     */
    Collection<Metric> getMetricsByTags(Map<String, String> tags);
    
    /**
     * 获取指标值映射
     * 
     * @return 指标名称到值的映射
     */
    Map<String, Object> getValues();
    
    /**
     * 获取所有指标名称
     * 
     * @return 指标名称集合
     */
    Set<String> getMetricNames();
    
    /**
     * 获取指标数量
     * 
     * @return 指标总数
     */
    int getMetricCount();
    
    /**
     * 按类型统计指标数量
     * 
     * @return 类型到数量的映射
     */
    Map<MetricType, Integer> getMetricCountsByType();
    
    /**
     * 检查是否包含指定指标
     * 
     * @param name 指标名称
     * @return 是否包含
     */
    boolean hasMetric(String name);
    
    /**
     * 获取快照生成耗时（毫秒）
     * 
     * @return 生成耗时
     */
    long getGenerationTime();
}