package com.muxin.gateway.refactory;

import java.time.Duration;
import java.util.List;

/**
 * 健康检查配置接口
 *
 * @author muxin
 */
public interface HealthCheckConfig {
    
    /**
     * 是否启用健康检查
     */
    boolean isEnabled();
    
    /**
     * 检查间隔
     */
    Duration getInterval();
    
    /**
     * 检查超时时间
     */
    Duration getTimeout();
    
    /**
     * 检查路径
     */
    String getPath();
    
    /**
     * 期望的状态码
     */
    List<Integer> getExpectedStatusCodes();
    
    /**
     * 连续失败次数阈值
     */
    int getFailureThreshold();
    
    /**
     * 连续成功次数阈值
     */
    int getSuccessThreshold();
} 