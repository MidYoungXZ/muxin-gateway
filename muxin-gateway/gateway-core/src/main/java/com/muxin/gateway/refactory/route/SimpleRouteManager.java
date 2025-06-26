package com.muxin.gateway.refactory.route;

import com.muxin.gateway.refactory.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 简单路由管理器实现
 *
 * @author muxin
 */
@Slf4j
public class SimpleRouteManager implements RouteManager {
    
    private final Map<String, UniversalRoute> routes = new ConcurrentHashMap<>();
    
    // Repository 接口实现
    @Override
    public UniversalRoute save(UniversalRoute entity) {
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
        
        UniversalRoute removed = routes.remove(id);
        if (removed != null) {
            log.info("移除路由: {} - {}", id, removed.getName());
        }
    }
    
    @Override
    public UniversalRoute findByUniqueCode(String id) {
        return routes.get(id);
    }
    
    @Override
    public Collection<UniversalRoute> findAll() {
        return new ArrayList<>(routes.values());
    }
    
    // 业务特定方法
    @Override
    public UniversalRoute matchRoute(UniversalRequestContext context) {
        if (context == null) {
            return null;
        }
        
        // 按优先级排序（order值越小优先级越高）
        List<UniversalRoute> sortedRoutes = routes.values().stream()
                .filter(UniversalRoute::isEnabled)
                .sorted(Comparator.comparingInt(UniversalRoute::getOrder))
                .collect(Collectors.toList());
        
        // 遍历路由进行匹配
        for (UniversalRoute route : sortedRoutes) {
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
     * 根据协议过滤路由
     */
    public List<UniversalRoute> getRoutesByProtocol(Protocol protocol) {
        if (protocol == null) {
            return Collections.emptyList();
        }
        
        return routes.values().stream()
                .filter(route -> route.getSupportedProtocols().contains(protocol))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取路由统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", routes.size());
        stats.put("enabledRoutes", routes.values().stream()
                .mapToInt(route -> route.isEnabled() ? 1 : 0)
                .sum());
        
        // 按协议分组统计
        Map<String, Long> protocolStats = routes.values().stream()
                .flatMap(route -> route.getSupportedProtocols().stream())
                .collect(Collectors.groupingBy(
                        Protocol::getName,
                        Collectors.counting()
                ));
        stats.put("protocolDistribution", protocolStats);
        
        return stats;
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