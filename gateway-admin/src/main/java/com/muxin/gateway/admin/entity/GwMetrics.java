package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控指标实体
 *
 * @author muxin
 */
@Data
@Table("gw_metrics")
public class GwMetrics {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 路由ID
     */
    private String routeId;
    
    /**
     * 请求总数
     */
    @Column("request_count")
    private Long requestCount;
    
    /**
     * 成功请求数
     */
    @Column("success_count")
    private Long successCount;
    
    /**
     * 失败请求数
     */
    @Column("failure_count")
    private Long failureCount;
    
    /**
     * 总耗时（毫秒）
     */
    @Column("total_time")
    private Long totalTime;
    
    /**
     * 平均耗时（毫秒）
     */
    @Column("avg_time")
    private Long avgTime;
    
    /**
     * 最大耗时（毫秒）
     */
    @Column("max_time")
    private Long maxTime;
    
    /**
     * 最小耗时（毫秒）
     */
    @Column("min_time")
    private Long minTime;
    
    /**
     * 采集时间
     */
    @Column("collect_time")
    private LocalDateTime collectTime;
} 