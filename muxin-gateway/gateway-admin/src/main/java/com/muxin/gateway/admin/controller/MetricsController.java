package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.MetricsQueryDTO;
import com.muxin.gateway.admin.model.vo.MetricsOverviewVO;
import com.muxin.gateway.admin.model.vo.MetricsVO;
import com.muxin.gateway.admin.model.vo.MetricsTrendVO;
import com.muxin.gateway.admin.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {
    
    private final MetricsService metricsService;
    
    /**
     * 获取监控概览
     */
    @GetMapping("/overview")
    @SaCheckPermission("monitor:view")
    public Result<MetricsOverviewVO> getOverview() {
        return Result.success(metricsService.getOverview());
    }
    
    /**
     * 获取路由监控详情
     */
    @GetMapping("/routes/{routeId}")
    @SaCheckPermission("monitor:view")
    public Result<List<MetricsVO>> getRouteMetrics(
            @PathVariable String routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "HOUR") String timeGranularity) {
        
        MetricsQueryDTO query = new MetricsQueryDTO();
        query.setRouteId(routeId);
        query.setStartTime(startTime != null ? startTime : LocalDateTime.now().minusDays(1));
        query.setEndTime(endTime != null ? endTime : LocalDateTime.now());
        query.setTimeGranularity(timeGranularity);
        
        return Result.success(metricsService.getRouteMetrics(routeId, query));
    }
    
    /**
     * 获取趋势分析
     */
    @GetMapping("/trends")
    @SaCheckPermission("monitor:view")
    public Result<List<MetricsTrendVO>> getTrends(
            @RequestParam(required = false) String routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "HOUR") String timeGranularity) {
        
        MetricsQueryDTO query = new MetricsQueryDTO();
        query.setRouteId(routeId);
        query.setStartTime(startTime != null ? startTime : LocalDateTime.now().minusDays(7));
        query.setEndTime(endTime != null ? endTime : LocalDateTime.now());
        query.setTimeGranularity(timeGranularity);
        
        return Result.success(metricsService.getTrends(query));
    }
    
    /**
     * 导出监控数据
     */
    @GetMapping("/export")
    @SaCheckPermission("monitor:export")
    public Result<String> exportMetrics(
            @RequestParam(required = false) String routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // TODO: 实现导出功能
        return Result.success("导出功能开发中");
    }
} 