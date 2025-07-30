package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指标趋势VO
 *
 * @author muxin
 */
@Data
public class MetricsTrendVO {
    
    /**
     * 时间点
     */
    private LocalDateTime timestamp;
    
    /**
     * 请求数
     */
    private Long requestCount;
    
    /**
     * 成功数
     */
    private Long successCount;
    
    /**
     * 失败数
     */
    private Long failureCount;
    
    /**
     * 平均响应时间
     */
    private Long avgResponseTime;
    
    /**
     * 成功率
     */
    private Double successRate;
} 