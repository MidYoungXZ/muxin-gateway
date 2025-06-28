package com.muxin.gateway.core.plus.monitor;

import java.util.Collection;

/**
 * 指标注册中心接口
 * 负责管理所有监控指标的注册、查询和收集
 * 
 * @author muxin
 */
public interface MetricsRegistry {
    
    /**
     * 注册计数器指标
     * 
     * @param name 指标名称
     * @param description 指标描述
     * @param tags 标签
     * @return 计数器
     */
    Counter registerCounter(String name, String description, String... tags);
    
    /**
     * 注册计量器指标
     * 
     * @param name 指标名称
     * @param description 指标描述
     * @param tags 标签
     * @return 计量器
     */
    Gauge registerGauge(String name, String description, String... tags);
    
    /**
     * 注册计时器指标
     * 
     * @param name 指标名称
     * @param description 指标描述
     * @param tags 标签
     * @return 计时器
     */
    Timer registerTimer(String name, String description, String... tags);
    
    /**
     * 注册直方图指标
     * 
     * @param name 指标名称
     * @param description 指标描述
     * @param tags 标签
     * @return 直方图
     */
    Histogram registerHistogram(String name, String description, String... tags);
    
    /**
     * 获取已注册的指标
     * 
     * @param name 指标名称
     * @return 指标对象，不存在则返回null
     */
    Metric getMetric(String name);
    
    /**
     * 移除指标
     * 
     * @param name 指标名称
     * @return 是否移除成功
     */
    boolean removeMetric(String name);
    
    /**
     * 获取所有指标
     * 
     * @return 所有指标的集合
     */
    Collection<Metric> getAllMetrics();
    
    /**
     * 获取指标快照
     * 
     * @return 当前所有指标的快照
     */
    MetricsSnapshot snapshot();
}