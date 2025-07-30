package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 指标统计VO
 *
 * @author muxin
 */
@Data
public class MetricsStatsVO {
    
    /**
     * 统计周期
     */
    private String period;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 请求总数
     */
    private Long totalRequests;
    
    /**
     * 成功请求数
     */
    private Long successRequests;
    
    /**
     * 失败请求数
     */
    private Long failureRequests;
    
    /**
     * 成功率
     */
    private Double successRate;
    
    /**
     * 平均响应时间
     */
    private Long avgResponseTime;
    
    /**
     * 最大响应时间
     */
    private Long maxResponseTime;
    
    /**
     * 最小响应时间
     */
    private Long minResponseTime;
    
    /**
     * 路由统计数据
     */
    private List<Map<String, Object>> routeStats;
} 