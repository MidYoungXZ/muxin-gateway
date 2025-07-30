package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 监控概览VO
 *
 * @author muxin
 */
@Data
@Builder
public class MetricsOverviewVO {
    
    /**
     * 今日请求总数
     */
    private Long todayRequestCount;
    
    /**
     * 今日成功率
     */
    private Double todaySuccessRate;
    
    /**
     * 今日平均响应时间（毫秒）
     */
    private Long todayAvgResponseTime;
    
    /**
     * 活跃路由数
     */
    private Long activeRouteCount;
    
    /**
     * 总请求数
     */
    private Long totalRequestCount;
    
    /**
     * 总成功率
     */
    private Double totalSuccessRate;
    
    /**
     * 请求数环比
     */
    private Double requestCountGrowth;
    
    /**
     * 成功率环比
     */
    private Double successRateGrowth;
    
    /**
     * 响应时间环比
     */
    private Double responseTimeGrowth;
    
    /**
     * 故障路由数
     */
    private Long faultRouteCount;
} 