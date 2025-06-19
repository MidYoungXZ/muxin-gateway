package com.muxin.gateway.admin.service.impl;

import com.muxin.gateway.admin.entity.GwMetrics;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.mapper.MetricsMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.model.dto.MetricsQueryDTO;
import com.muxin.gateway.admin.model.vo.MetricsOverviewVO;
import com.muxin.gateway.admin.model.vo.MetricsTrendVO;
import com.muxin.gateway.admin.model.vo.MetricsVO;
import com.muxin.gateway.admin.model.vo.RouteMetricsVO;
import com.muxin.gateway.admin.service.MetricsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.muxin.gateway.admin.entity.table.Tables.GW_METRICS;
import static com.muxin.gateway.admin.entity.table.Tables.GW_ROUTE;

/**
 * 监控服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl extends ServiceImpl<MetricsMapper, GwMetrics> implements MetricsService {
    
    private final MetricsMapper metricsMapper;
    private final RouteMapper routeMapper;
    
    @Override
    public MetricsOverviewVO getOverview() {
        // 获取实时指标
        Map<String, Object> realTimeMetrics = getRealTimeMetrics();
        
        // 计算今日数据
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        QueryWrapper todayWrapper = QueryWrapper.create()
                .select()
                .from(GW_METRICS)
                .where(GW_METRICS.COLLECT_TIME.between(todayStart, now));
        
        List<GwMetrics> todayMetrics = list(todayWrapper);
        
        Long todayRequestCount = todayMetrics.stream()
                .mapToLong(GwMetrics::getRequestCount)
                .sum();
        Long todaySuccessCount = todayMetrics.stream()
                .mapToLong(GwMetrics::getSuccessCount)
                .sum();
        Long todayTotalTime = todayMetrics.stream()
                .mapToLong(GwMetrics::getTotalTime)
                .sum();
        
        Double todaySuccessRate = 100.0;
        Long todayAvgResponseTime = 0L;
        
        if (todayRequestCount > 0) {
            todaySuccessRate = (todaySuccessCount * 100.0) / todayRequestCount;
            todayAvgResponseTime = todayTotalTime / todayRequestCount;
        }
        
        // 构建返回对象
        return MetricsOverviewVO.builder()
                .todayRequestCount(todayRequestCount)
                .todaySuccessRate(todaySuccessRate)
                .todayAvgResponseTime(todayAvgResponseTime)
                .activeRouteCount((Long) realTimeMetrics.get("activeRoutes"))
                .totalRequestCount(todayRequestCount) // 暂时使用今日数据
                .totalSuccessRate(todaySuccessRate)
                .requestCountGrowth(0.0) // TODO: 计算环比
                .successRateGrowth(0.0)
                .responseTimeGrowth(0.0)
                .faultRouteCount(0L) // TODO: 计算故障路由
                .build();
    }
    
    @Override
    public List<MetricsVO> getRouteMetrics(String routeId, MetricsQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_METRICS)
                .where(GW_METRICS.ROUTE_ID.eq(routeId));
        
        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.and(GW_METRICS.COLLECT_TIME.ge(query.getStartTime()));
        }
        
        if (query.getEndTime() != null) {
            wrapper.and(GW_METRICS.COLLECT_TIME.le(query.getEndTime()));
        }
        
        // 排序
        wrapper.orderBy(GW_METRICS.COLLECT_TIME.desc());
        
        // 查询
        List<GwMetrics> metricsList = list(wrapper);
        
        // 转换为VO
        return metricsList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MetricsTrendVO> getTrends(MetricsQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_METRICS);
        
        // 路由过滤
        if (StringUtils.hasText(query.getRouteId())) {
            wrapper.where(GW_METRICS.ROUTE_ID.eq(query.getRouteId()));
        }
        
        // 时间范围
        LocalDateTime startTime = query.getStartTime();
        LocalDateTime endTime = query.getEndTime();
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        wrapper.and(GW_METRICS.COLLECT_TIME.between(startTime, endTime));
        wrapper.orderBy(GW_METRICS.COLLECT_TIME.asc());
        
        // 查询数据
        List<GwMetrics> metricsList = list(wrapper);
        
        // 按小时分组统计
        Map<LocalDateTime, List<GwMetrics>> hourlyGroup = metricsList.stream()
                .collect(Collectors.groupingBy(m -> 
                    m.getCollectTime().truncatedTo(ChronoUnit.HOURS)));
        
        // 构建趋势数据
        List<MetricsTrendVO> trends = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<GwMetrics>> entry : hourlyGroup.entrySet()) {
            MetricsTrendVO trend = new MetricsTrendVO();
            trend.setTimestamp(entry.getKey());
            
            List<GwMetrics> hourMetrics = entry.getValue();
            trend.setRequestCount(hourMetrics.stream()
                    .mapToLong(GwMetrics::getRequestCount)
                    .sum());
            trend.setSuccessCount(hourMetrics.stream()
                    .mapToLong(GwMetrics::getSuccessCount)
                    .sum());
            trend.setFailureCount(hourMetrics.stream()
                    .mapToLong(GwMetrics::getFailureCount)
                    .sum());
            
            // 计算成功率
            if (trend.getRequestCount() > 0) {
                BigDecimal successRate = new BigDecimal(trend.getSuccessCount())
                        .divide(new BigDecimal(trend.getRequestCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                trend.setSuccessRate(successRate.doubleValue());
            } else {
                trend.setSuccessRate(100.0);
            }
            
            // 计算平均响应时间
            Long totalTime = hourMetrics.stream()
                    .mapToLong(GwMetrics::getTotalTime)
                    .sum();
            Long totalCount = hourMetrics.stream()
                    .mapToLong(GwMetrics::getRequestCount)
                    .sum();
            
            if (totalCount > 0) {
                trend.setAvgResponseTime(totalTime / totalCount);
            } else {
                trend.setAvgResponseTime(0L);
            }
            
            trends.add(trend);
        }
        
        // 按时间排序
        trends.sort(Comparator.comparing(MetricsTrendVO::getTimestamp));
        
        return trends;
    }
    
    @Override
    public void recordMetrics(String routeId, long responseTime, boolean success) {
        // 获取当前分钟的时间点
        LocalDateTime collectTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        
        // 查询是否已有该时间点的记录
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_METRICS)
                .where(GW_METRICS.ROUTE_ID.eq(routeId))
                .and(GW_METRICS.COLLECT_TIME.eq(collectTime))
                .limit(1);
        
        GwMetrics metrics = getOne(wrapper);
        
        if (metrics == null) {
            // 创建新记录
            metrics = new GwMetrics();
            metrics.setRouteId(routeId);
            metrics.setCollectTime(collectTime);
            metrics.setRequestCount(1L);
            metrics.setSuccessCount(success ? 1L : 0L);
            metrics.setFailureCount(success ? 0L : 1L);
            metrics.setTotalTime(responseTime);
            metrics.setMaxTime(responseTime);
            metrics.setMinTime(responseTime);
            
            save(metrics);
        } else {
            // 更新现有记录
            metrics.setRequestCount(metrics.getRequestCount() + 1);
            if (success) {
                metrics.setSuccessCount(metrics.getSuccessCount() + 1);
            } else {
                metrics.setFailureCount(metrics.getFailureCount() + 1);
            }
            metrics.setTotalTime(metrics.getTotalTime() + responseTime);
            metrics.setMaxTime(Math.max(metrics.getMaxTime(), responseTime));
            metrics.setMinTime(Math.min(metrics.getMinTime(), responseTime));
            
            updateById(metrics);
        }
    }
    
    @Override
    public void cleanExpiredData(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(GW_METRICS)
                .where(GW_METRICS.COLLECT_TIME.lt(cutoffTime));
        
        long deletedCount = metricsMapper.deleteByQuery(deleteWrapper);
        
        log.info("清理了 {} 条超过 {} 天的监控数据", deletedCount, retentionDays);
    }
    
    /**
     * 获取路由指标列表
     */
    private List<RouteMetricsVO> getRouteMetricsList() {
        // 获取所有路由
        QueryWrapper routeWrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE)
                .where(GW_ROUTE.DELETED.eq(0))
                .and(GW_ROUTE.ENABLED.eq(1))
                .orderBy(GW_ROUTE.ORDER.asc());
        
        List<GwRoute> routes = routeMapper.selectListByQuery(routeWrapper);
        
        // 构建路由指标列表
        List<RouteMetricsVO> voList = new ArrayList<>();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        
        for (GwRoute route : routes) {
            RouteMetricsVO vo = new RouteMetricsVO();
            vo.setRouteId(route.getRouteId());
            vo.setRouteName(route.getRouteName());
            
            // 查询该路由24小时内的指标
            QueryWrapper metricsWrapper = QueryWrapper.create()
                    .select()
                    .from(GW_METRICS)
                    .where(GW_METRICS.ROUTE_ID.eq(route.getRouteId()))
                    .and(GW_METRICS.COLLECT_TIME.between(startTime, endTime));
            
            List<GwMetrics> metricsList = list(metricsWrapper);
            
            // 计算汇总数据
            if (!metricsList.isEmpty()) {
                vo.setTotalRequests(metricsList.stream()
                        .mapToLong(GwMetrics::getRequestCount)
                        .sum());
                vo.setSuccessRequests(metricsList.stream()
                        .mapToLong(GwMetrics::getSuccessCount)
                        .sum());
                vo.setFailureRequests(metricsList.stream()
                        .mapToLong(GwMetrics::getFailureCount)
                        .sum());
                
                // 计算成功率
                if (vo.getTotalRequests() > 0) {
                    BigDecimal successRate = new BigDecimal(vo.getSuccessRequests())
                            .divide(new BigDecimal(vo.getTotalRequests()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    vo.setSuccessRate(successRate.doubleValue());
                } else {
                    vo.setSuccessRate(0.0);
                }
                
                // 计算平均响应时间
                Long totalTime = metricsList.stream()
                        .mapToLong(GwMetrics::getTotalTime)
                        .sum();
                Long totalCount = metricsList.stream()
                        .mapToLong(GwMetrics::getRequestCount)
                        .sum();
                
                if (totalCount > 0) {
                    vo.setAvgResponseTime(totalTime / totalCount);
                } else {
                    vo.setAvgResponseTime(0L);
                }
                
                // 最新采集时间
                vo.setLastCollectTime(metricsList.stream()
                        .map(GwMetrics::getCollectTime)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
            } else {
                vo.setTotalRequests(0L);
                vo.setSuccessRequests(0L);
                vo.setFailureRequests(0L);
                vo.setSuccessRate(100.0);
                vo.setAvgResponseTime(0L);
            }
            
            voList.add(vo);
        }
        
        return voList;
    }
    
    /**
     * 获取实时指标
     */
    private Map<String, Object> getRealTimeMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最近5分钟的数据
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5);
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_METRICS)
                .where(GW_METRICS.COLLECT_TIME.between(startTime, endTime))
                .orderBy(GW_METRICS.COLLECT_TIME.desc());
        
        List<GwMetrics> recentMetrics = list(wrapper);
        
        // 实时QPS（最近一分钟）
        LocalDateTime oneMinuteAgo = endTime.minusMinutes(1);
        Long recentRequests = recentMetrics.stream()
                .filter(m -> m.getCollectTime().isAfter(oneMinuteAgo))
                .mapToLong(GwMetrics::getRequestCount)
                .sum();
        result.put("qps", recentRequests / 60.0);
        
        // 实时成功率
        Long recentSuccesses = recentMetrics.stream()
                .mapToLong(GwMetrics::getSuccessCount)
                .sum();
        Long recentTotal = recentMetrics.stream()
                .mapToLong(GwMetrics::getRequestCount)
                .sum();
        
        if (recentTotal > 0) {
            BigDecimal successRate = new BigDecimal(recentSuccesses)
                    .divide(new BigDecimal(recentTotal), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            result.put("successRate", successRate.doubleValue());
        } else {
            result.put("successRate", 100.0);
        }
        
        // 活跃路由数
        long activeRoutes = recentMetrics.stream()
                .map(GwMetrics::getRouteId)
                .distinct()
                .count();
        result.put("activeRoutes", activeRoutes);
        
        // 平均响应时间
        if (!recentMetrics.isEmpty()) {
            Long totalTime = recentMetrics.stream()
                    .mapToLong(GwMetrics::getTotalTime)
                    .sum();
            Long totalCount = recentMetrics.stream()
                    .mapToLong(GwMetrics::getRequestCount)
                    .sum();
            
            if (totalCount > 0) {
                result.put("avgResponseTime", totalTime / totalCount);
            } else {
                result.put("avgResponseTime", 0);
            }
        } else {
            result.put("avgResponseTime", 0);
        }
        
        // 最新采集时间
        recentMetrics.stream()
                .map(GwMetrics::getCollectTime)
                .max(LocalDateTime::compareTo)
                .ifPresent(time -> result.put("lastCollectTime", time));
        
        return result;
    }
    
    /**
     * 转换为VO
     */
    private MetricsVO convertToVO(GwMetrics metrics) {
        MetricsVO vo = new MetricsVO();
        BeanUtils.copyProperties(metrics, vo);
        
        // 计算成功率
        if (metrics.getRequestCount() > 0) {
            BigDecimal successRate = new BigDecimal(metrics.getSuccessCount())
                    .divide(new BigDecimal(metrics.getRequestCount()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            vo.setSuccessRate(successRate.doubleValue());
        } else {
            vo.setSuccessRate(100.0);
        }
        
        // 查询路由名称
        QueryWrapper routeWrapper = QueryWrapper.create()
                .select(GW_ROUTE.ROUTE_NAME)
                .from(GW_ROUTE)
                .where(GW_ROUTE.ROUTE_ID.eq(metrics.getRouteId()))
                .limit(1);
        
        GwRoute route = routeMapper.selectOneByQuery(routeWrapper);
        if (route != null) {
            vo.setRouteName(route.getRouteName());
        }
        
        return vo;
    }
} 