package com.muxin.gateway.core.plus.monitor;

/**
 * 直方图接口
 * 用于测量数值分布，记录一系列数值的统计信息
 * 典型用法：响应大小分布、请求延迟分布、负载分布等
 * 
 * @author muxin
 */
public interface Histogram extends Metric {
    
    /**
     * 记录一个值
     * 
     * @param value 要记录的数值
     */
    void record(double value);
    
    /**
     * 批量记录多个值
     * 
     * @param values 要记录的数值数组
     */
    void record(double... values);
    
    /**
     * 获取记录次数
     * 
     * @return 记录次数
     */
    long getCount();
    
    /**
     * 获取所有值的总和
     * 
     * @return 总和
     */
    double getSum();
    
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
     * 获取指定百分位数的值
     * 
     * @param percentile 百分位数（0-100）
     * @return 百分位数值
     * @throws IllegalArgumentException 如果percentile不在0-100范围内
     */
    double getPercentile(double percentile);
    
    /**
     * 获取中位数（50%分位数）
     * 
     * @return 中位数
     */
    double getMedian();
    
    /**
     * 获取直方图快照
     * 
     * @return 直方图快照
     */
    HistogramSnapshot snapshot();
    
    /**
     * 清空所有记录的值
     * 注意：此操作将重置所有统计信息
     */
    void clear();
    
    /**
     * 获取记录的值数量是否达到上限
     * 
     * @return 是否已满
     */
    boolean isFull();
    
    /**
     * 获取可记录的最大值数量
     * 
     * @return 最大容量
     */
    int getCapacity();
}