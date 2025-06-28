package com.muxin.gateway.core.plus.monitor;

/**
 * 计时样本接口
 * 用于测量代码执行时间
 * 
 * @author muxin
 */
public interface TimingSample {
    
    /**
     * 停止计时并记录
     * 
     * @return 消耗时间（毫秒）
     */
    long stop();
    
    /**
     * 停止计时并记录到指定计时器
     * 
     * @param timer 目标计时器
     * @return 消耗时间（毫秒）
     */
    long stop(Timer timer);
    
    /**
     * 获取当前已消耗时间（不停止计时）
     * 
     * @return 已消耗时间（毫秒）
     */
    long elapsed();
    
    /**
     * 检查计时是否正在进行
     * 
     * @return true表示正在计时
     */
    boolean isRunning();
}