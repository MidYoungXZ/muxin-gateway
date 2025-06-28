package com.muxin.gateway.core.plus.monitor;

import java.time.Duration;

/**
 * 计时器接口
 * 用于测量时间分布
 * 
 * @author muxin
 */
public interface Timer extends Metric {
    
    /**
     * 开始计时
     * 
     * @return 计时样本
     */
    TimingSample start();
    
    /**
     * 记录时间
     * 
     * @param duration 持续时间
     */
    void record(Duration duration);
    
    /**
     * 记录时间（毫秒）
     * 
     * @param milliseconds 毫秒数
     */
    void record(long milliseconds);
    
    /**
     * 获取计时器快照
     * 
     * @return 计时器快照
     */
    TimerSnapshot snapshot();
    
    /**
     * 获取总计时次数
     * 
     * @return 计时次数
     */
    long getCount();
    
    /**
     * 获取总计时时间
     * 
     * @return 总时间（毫秒）
     */
    double getTotalTime();
    
    /**
     * 获取平均时间
     * 
     * @return 平均时间（毫秒）
     */
    double getMean();
} 