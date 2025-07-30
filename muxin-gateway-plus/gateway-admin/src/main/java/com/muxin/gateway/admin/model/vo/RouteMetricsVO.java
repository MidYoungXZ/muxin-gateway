package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 路由指标VO
 *
 * @author muxin
 */
@Data
public class RouteMetricsVO {
    
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
     * 平均响应时间（毫秒）
     */
    private Long avgResponseTime;
    
    /**
     * 最新采集时间
     */
    private LocalDateTime lastCollectTime;
} 