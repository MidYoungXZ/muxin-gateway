package com.muxin.gateway.core.plus.monitor;

/**
 * 计数器接口
 * 用于累计计数，只能递增，不能递减
 * 典型用法：请求数量、错误数量、成功数量等累计统计
 * 
 * @author muxin
 */
public interface Counter extends Metric {
    
    /**
     * 递增计数（默认+1）
     */
    void increment();
    
    /**
     * 递增指定数量
     * 
     * @param amount 递增数量（必须 >= 0）
     * @throws IllegalArgumentException 如果amount < 0
     */
    void increment(double amount);
    
    /**
     * 获取当前计数值
     * 
     * @return 计数值
     */
    double getCount();
    
    /**
     * 重置计数器为0
     * 注意：此操作可能影响监控数据的连续性
     */
    void reset();
    
    /**
     * 获取计数器的增长率（每秒）
     * 
     * @return 每秒增长率
     */
    double getRate();
    
    /**
     * 获取自创建以来的平均增长率
     * 
     * @return 平均增长率（每秒）
     */
    double getMeanRate();
}