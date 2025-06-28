package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.monitor.MonitorMetadata;
import com.muxin.gateway.core.plus.monitor.MonitorType;
import com.muxin.gateway.core.plus.monitor.MetricsRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用路由管理器实现
 *
 * @author muxin
 */
@Slf4j
public class DefaultRouteManager implements RouteManager {
    
    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    
    // Repository 接口实现
    @Override
    public Route save(Route entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Route and route ID cannot be null");
        }
        
        routes.put(entity.getId(), entity);
        log.info("保存路由: {} - {}", entity.getId(), entity.getName());
        return entity;
    }
    
    @Override
    public void removeByUniqueCode(String id) {
        if (id == null) {
            return;
        }
        
        Route removed = routes.remove(id);
        if (removed != null) {
            log.info("移除路由: {} - {}", id, removed.getName());
        }
    }
    
    @Override
    public Route findByUniqueCode(String id) {
        return routes.get(id);
    }
    
    @Override
    public Collection<Route> findAll() {
        return new ArrayList<>(routes.values());
    }
    
    // 业务特定方法
    @Override
    public Route matchRoute(RequestContext context) {
        if (context == null) {
            return null;
        }
        
        // 按优先级排序（order值越小优先级越高）
        List<Route> sortedRoutes = routes.values().stream()
                .filter(Route::isEnabled)
                .sorted(Comparator.comparingInt(Route::getOrder))
                .toList();
        
        // 遍历路由进行匹配
        for (Route route : sortedRoutes) {
            try {
                if (route.matches(context)) {
                    log.debug("匹配路由: {} 用于请求", route.getId());
                    return route;
                }
            } catch (Exception e) {
                log.error("路由匹配错误 {}: {}", route.getId(), e.getMessage());
            }
        }
        
        log.debug("请求未匹配到任何路由");
        return null;
    }
    
    /**
     * 获取启用的路由数量
     */
    public int getEnabledRouteCount() {
        return (int) routes.values().stream()
                .filter(Route::isEnabled)
                .count();
    }
    
    // getStatistics() 方法已移除 - 使用统一监控接口

    // ========== Monitorable 接口实现 ==========

    @Override
    public String getMonitorId() {
        return "route-manager";
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.ROUTE_MANAGER;
    }

    @Override
    public void registerMetrics(MetricsRegistry registry) {
        // 注册路由相关指标
        log.info("[UniversalRouteManager] 监控指标注册完成: {}", getMonitorId());
    }

    @Override
    public MonitorMetadata getMonitorMetadata() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}