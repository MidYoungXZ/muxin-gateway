package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控查询DTO
 *
 * @author muxin
 */
@Data
public class MetricsQueryDTO {
    
    /**
     * 路由ID
     */
    private String routeId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 时间粒度：MINUTE-分钟，HOUR-小时，DAY-天
     */
    private String timeGranularity = "HOUR";
}