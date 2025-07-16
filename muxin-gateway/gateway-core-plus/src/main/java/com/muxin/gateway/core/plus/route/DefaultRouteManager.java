package com.muxin.gateway.core.plus.route;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 默认路由管理器实现
 * 负责路由规则管理和匹配，提供高性能的路由查找能力
 * 
 * @author muxin
 */
@Slf4j
public class DefaultRouteManager implements RouteManager {

    // 路由存储：按ID索引
    private final Map<String, Route> routeStorage = new ConcurrentHashMap<>();
    
    // 路由缓存：按协议类型分组，按优先级排序
    private final Map<String, List<Route>> routeCache = new ConcurrentHashMap<>();
    
    // 读写锁，用于缓存更新
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    // 路由变更监听器
    private final List<RouteChangeListener> listeners = new ArrayList<>();
    
    // 生命周期状态
    private volatile boolean initialized = false;
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    @Override
    public void init() {
        if (!initialized) {
            log.info("[DefaultRouteManager] 路由管理器初始化开始");
            
            // 初始化时可以加载默认路由或从配置中心加载
            loadInitialRoutes();
            
            initialized = true;
            log.info("[DefaultRouteManager] 路由管理器初始化完成");
        }
    }

    @Override
    public void start() {
        if (!initialized) {
            throw new IllegalStateException("路由管理器未初始化");
        }
        
        if (!started) {
            log.info("[DefaultRouteManager] 路由管理器启动");
            started = true;
        }
    }

    @Override
    public void shutdown() {
        if (!shutdown) {
            log.info("[DefaultRouteManager] 路由管理器关闭");
            shutdown = true;
            started = false;
            
            // 清理资源
            routeStorage.clear();
            routeCache.clear();
            listeners.clear();
        }
    }

    @Override
    public Route matchRoute(RequestContext context) {
        if (!started || shutdown) {
            throw new IllegalStateException("路由管理器未启动或已关闭");
        }
        try {

            return null;
        } catch (Exception e) {
            log.error("[DefaultRouteManager] 路由匹配异常", e);
            return null;
        }
    }

    @Override
    public Route insert(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("路由不能为空");
        }

        validateRoute(route);

        try {
            cacheLock.writeLock().lock();
            
            // 存储路由
            Route existingRoute = routeStorage.put(route.getId(), route);
            
            // 更新缓存
            refreshCache();
            
            // 通知监听器
            notifyRouteAdded(route);
            
            log.info("[DefaultRouteManager] 路由添加成功: {} - {}", 
                route.getId(), route.getProtocolType());
            
            return existingRoute;
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String routeId) {
        if (routeId == null) {
            return;
        }

        try {
            cacheLock.writeLock().lock();
            
            Route removedRoute = routeStorage.remove(routeId);
            if (removedRoute != null) {
                // 更新缓存
                refreshCache();
                
                // 通知监听器
                notifyRouteRemoved(removedRoute);
                
                log.info("[DefaultRouteManager] 路由删除成功: {}", routeId);
            } else {
                log.debug("[DefaultRouteManager] 未找到要删除的路由: {}", routeId);
            }
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public Route selectById(String routeId) {
        return routeStorage.get(routeId);
    }

    @Override
    public Collection<Route> selectAll() {
        return new ArrayList<>(routeStorage.values());
    }

    /**
     * 获取指定协议的路由列表（已按优先级排序）
     */
    public List<Route> getRoutesByProtocol(String protocolType) {
        try {
            cacheLock.readLock().lock();
            return routeCache.getOrDefault(protocolType, Collections.emptyList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取所有协议类型
     */
    public Set<String> getSupportedProtocols() {
        try {
            cacheLock.readLock().lock();
            return new HashSet<>(routeCache.keySet());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 添加路由变更监听器
     */
    public void addRouteChangeListener(RouteChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除路由变更监听器
     */
    public void removeRouteChangeListener(RouteChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 刷新路由缓存
     */
    private void refreshCache() {
        // 清空旧缓存
        routeCache.clear();
        
        // 按协议类型分组并排序
        Map<String, List<Route>> groupedRoutes = routeStorage.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Route::getProtocolType));
        
        // 对每个协议的路由按优先级排序
        for (Map.Entry<String, List<Route>> entry : groupedRoutes.entrySet()) {
            List<Route> sortedRoutes = entry.getValue().stream()
                .sorted(Comparator.comparingInt(Route::getOrder))
                .collect(Collectors.toList());
            routeCache.put(entry.getKey(), sortedRoutes);
        }
        
        log.debug("[DefaultRouteManager] 路由缓存刷新完成，协议数: {}, 总路由数: {}", 
            routeCache.size(), routeStorage.size());
    }

    /**
     * 验证路由配置
     */
    private void validateRoute(Route route) {
        if (route.getId() == null || route.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        
        if (route.getSupportedProtocol() == null) {
            throw new IllegalArgumentException("路由必须指定支持的协议");
        }
        
        if (!route.isConfigurationValid()) {
            throw new IllegalArgumentException("路由配置无效，协议不一致: " + route.getId());
        }
        
        if (route.getPredicates() == null || route.getPredicates().isEmpty()) {
            throw new IllegalArgumentException("路由必须至少包含一个断言: " + route.getId());
        }
    }

    /**
     * 加载初始路由
     */
    private void loadInitialRoutes() {
        // 这里可以从配置文件、数据库或配置中心加载初始路由
        log.debug("[DefaultRouteManager] 加载初始路由配置");
    }

    /**
     * 通知路由添加事件
     */
    private void notifyRouteAdded(Route route) {
        for (RouteChangeListener listener : listeners) {
            try {
                listener.onRouteAdded(route);
            } catch (Exception e) {
                log.warn("[DefaultRouteManager] 路由变更监听器异常", e);
            }
        }
    }

    /**
     * 通知路由移除事件
     */
    private void notifyRouteRemoved(Route route) {
        for (RouteChangeListener listener : listeners) {
            try {
                listener.onRouteRemoved(route);
            } catch (Exception e) {
                log.warn("[DefaultRouteManager] 路由变更监听器异常", e);
            }
        }
    }



    /**
     * 路由变更监听器接口
     */
    public interface RouteChangeListener {
        /**
         * 路由添加事件
         */
        default void onRouteAdded(Route route) {}

        /**
         * 路由移除事件
         */
        default void onRouteRemoved(Route route) {}

        /**
         * 路由更新事件
         */
        default void onRouteUpdated(Route oldRoute, Route newRoute) {}
    }
} 