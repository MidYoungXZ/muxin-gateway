package com.muxin.gateway.core.plus.monitor;

/**
 * 直方图快照接口
 * 提供直方图在某个时刻的统计信息
 * 
 * @author muxin
 */
public interface HistogramSnapshot {
    
    /**
     * 获取样本数量
     * 
     * @return 样本数量
     */
    long getCount();
    
    /**
     * 获取平均值
     * 
     * @return 平均值
     */
    double getMean();
    
    /**
     * 获取最大值
     * 
     * @return 最大值
     */
    double getMax();
    
    /**
     * 获取最小值
     * 
     * @return 最小值
     */
    double getMin();
    
    /**
     * 获取总和
     * 
     * @return 总和
     */
    double getSum();
    
    /**
     * 获取指定百分位数的值
     * 
     * @param percentile 百分位数（0-100）
     * @return 百分位值
     */
    double getPercentile(double percentile);
    
    /**
     * 获取标准差
     * 
     * @return 标准差
     */
    double getStdDev();
    
    /**
     * 获取方差
     * 
     * @return 方差
     */
    double getVariance();
    
    /**
     * 获取中位数
     * 
     * @return 中位数
     */
    double getMedian();
    
    /**
     * 获取快照时间戳
     * 
     * @return 时间戳（毫秒）
     */
    long getTimestamp();
}