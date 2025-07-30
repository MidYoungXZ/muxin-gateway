package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控指标VO
 *
 * @author muxin
 */
@Data
public class MetricsVO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 路由ID
     */
    private String routeId;
    
    /**
     * 路由名称
     */
    private String routeName;
    
    /**
     * 请求总数
     */
    private Long requestCount;
    
    /**
     * 成功请求数
     */
    private Long successCount;
    
    /**
     * 失败请求数
     */
    private Long failureCount;
    
    /**
     * 成功率
     */
    private Double successRate;
    
    /**
     * 平均耗时（毫秒）
     */
    private Long avgTime;
    
    /**
     * 最大耗时（毫秒）
     */
    private Long maxTime;
    
    /**
     * 最小耗时（毫秒）
     */
    private Long minTime;
    
    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
} 