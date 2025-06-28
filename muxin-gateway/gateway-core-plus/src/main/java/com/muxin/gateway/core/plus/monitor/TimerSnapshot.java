package com.muxin.gateway.core.plus.monitor;

/**
 * 计时器快照接口
 * 提供计时器在某个时刻的统计信息
 * 
 * @author muxin
 */
public interface TimerSnapshot {
    
    /**
     * 获取计时次数
     * 
     * @return 计时次数
     */
    long getCount();
    
    /**
     * 获取平均时间（毫秒）
     * 
     * @return 平均时间
     */
    double getMean();
    
    /**
     * 获取最大时间（毫秒）
     * 
     * @return 最大时间
     */
    double getMax();
    
    /**
     * 获取最小时间（毫秒）
     * 
     * @return 最小时间
     */
    double getMin();
    
    /**
     * 获取总时间（毫秒）
     * 
     * @return 总时间
     */
    double getTotalTime();
    
    /**
     * 获取指定百分位数的时间
     * 
     * @param percentile 百分位数（0-100）
     * @return 百分位时间（毫秒）
     */
    double getPercentile(double percentile);
    
    /**
     * 获取标准差
     * 
     * @return 标准差
     */
    double getStdDev();
    
    /**
     * 获取快照时间戳
     * 
     * @return 时间戳（毫秒）
     */
    long getTimestamp();
}