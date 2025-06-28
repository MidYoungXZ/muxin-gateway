package com.muxin.gateway.core.plus.monitor;

import java.util.function.Supplier;

/**
 * 计量器接口
 * 用于表示瞬时值，可以任意变化（增加或减少）
 * 典型用法：当前连接数、内存使用量、队列长度等瞬时指标
 * 
 * @author muxin
 */
public interface Gauge extends Metric {
    
    /**
     * 设置当前值
     * 
     * @param value 新值
     */
    void setValue(double value);
    
    /**
     * 获取当前数值
     * 
     * @return 当前数值
     */
    double getDoubleValue();
    
    /**
     * 递增值
     * 
     * @param amount 递增数量
     */
    void increment(double amount);
    
    /**
     * 递增值（默认+1）
     */
    void increment();
    
    /**
     * 递减值
     * 
     * @param amount 递减数量
     */
    void decrement(double amount);
    
    /**
     * 递减值（默认-1）
     */
    void decrement();
    
    /**
     * 设置值提供器（用于动态计算值）
     * 
     * @param valueSupplier 值提供器
     */
    void setValueSupplier(Supplier<Double> valueSupplier);
    
    /**
     * 获取最大值（自创建以来）
     * 
     * @return 最大值
     */
    double getMaxValue();
    
    /**
     * 获取最小值（自创建以来）
     * 
     * @return 最小值
     */
    double getMinValue();
    
    /**
     * 重置最大值和最小值记录
     */
    void resetExtremes();
}