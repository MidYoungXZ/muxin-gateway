package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.MetricsQueryDTO;
import com.muxin.gateway.admin.model.vo.MetricsOverviewVO;
import com.muxin.gateway.admin.model.vo.MetricsVO;
import com.muxin.gateway.admin.model.vo.MetricsTrendVO;

import java.util.List;

/**
 * 监控指标服务接口
 *
 * @author muxin
 */
public interface MetricsService {
    
    /**
     * 获取监控概览
     */
    MetricsOverviewVO getOverview();
    
    /**
     * 获取路由监控详情
     */
    List<MetricsVO> getRouteMetrics(String routeId, MetricsQueryDTO query);
    
    /**
     * 获取趋势分析
     */
    List<MetricsTrendVO> getTrends(MetricsQueryDTO query);
    
    /**
     * 记录指标
     */
    void recordMetrics(String routeId, long responseTime, boolean success);
    
    /**
     * 清理过期数据
     */
    void cleanExpiredData(int retentionDays);
} 