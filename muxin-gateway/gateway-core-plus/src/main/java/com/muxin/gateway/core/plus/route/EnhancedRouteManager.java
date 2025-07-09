package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.config.GatewayConfigLoader;
import com.muxin.gateway.core.plus.config.GatewayRouteConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 增强的路由管理器
 * 支持YAML配置和动态路由管理
 *
 * @author muxin
 */
@Slf4j
public class EnhancedRouteManager implements RouteManager {


    private final GatewayConfigLoader configLoader;
    private final RouteConfigConverter configConverter;
    private final ConcurrentMap<String, EnhancedRoute> routes;
    private volatile List<EnhancedRoute> sortedRoutes;
    private final GlobalRouteConfig globalConfig;

    public EnhancedRouteManager() {
        this.configLoader = new GatewayConfigLoader();
        this.configConverter = new RouteConfigConverter();
        this.routes = new ConcurrentHashMap<>();
        this.sortedRoutes = new ArrayList<>();
        this.globalConfig = GlobalRouteConfig.defaultConfig();
    }
    
    public EnhancedRouteManager(GlobalRouteConfig globalConfig) {
        this.configLoader = new GatewayConfigLoader();
        this.configConverter = new RouteConfigConverter();
        this.routes = new ConcurrentHashMap<>();
        this.sortedRoutes = new ArrayList<>();
        this.globalConfig = globalConfig != null ? globalConfig : GlobalRouteConfig.defaultConfig();
    }

    
    // ========== Repository接口实现 ==========
    
    @Override
    public Route save(Route route) {
        if (route instanceof EnhancedRoute enhancedRoute) {
            addRoute(enhancedRoute);
            return route;
        }
        throw new IllegalArgumentException("只支持EnhancedRoute类型");
    }
    
    @Override
    public Route findByUniqueCode(String routeId) {
        return getRoute(routeId);
    }
    
    @Override
    public void removeByUniqueCode(String routeId) {
        EnhancedRoute removed = routes.remove(routeId);
        if (removed != null) {
            updateSortedRoutes();
        }
    }
    
    @Override
    public List<Route> findAll() {
        return new ArrayList<>(sortedRoutes);
    }
    

    
    @Override
    public void init() {
        try {
            log.info("初始化增强路由管理器...");
            
            // 加载配置文件
            loadConfiguration();
            
            log.info("增强路由管理器初始化完成，共加载 {} 个路由", routes.size());
            
        } catch (Exception e) {
            log.error("增强路由管理器初始化失败", e);
            throw new RuntimeException("路由管理器初始化失败", e);
        }
    }
    
    @Override
    public void start() {
        log.info("启动增强路由管理器...");
        // 路由管理器无需特殊启动操作
        log.info("增强路由管理器启动完成");
    }
    
    @Override
    public void shutdown() {
        log.info("关闭增强路由管理器...");
        routes.clear();
        sortedRoutes = new ArrayList<>();
        log.info("增强路由管理器关闭完成");
    }
    
    @Override
    public Route matchRoute(RequestContext context) {
        if (context == null) {
            return null;
        }
        
        // 按优先级顺序匹配路由
        for (EnhancedRoute route : sortedRoutes) {
            if (route.matches(context)) {
                log.debug("匹配到路由: {} ({})", route.getId(), route.getName());
                return route;
            }
        }
        
        log.debug("未找到匹配的路由");
        return null;
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfiguration() {
        try {
            // 加载YAML配置
            GatewayRouteConfig config = configLoader.loadConfig();
            
            if (config.getRoutes() != null && !config.getRoutes().isEmpty()) {
                // 添加路由定义（会自动应用全局配置）
                for (RouteDefinition routeDefinition : config.getRoutes()) {
                    addRouteDefinition(routeDefinition);
                }
                
                log.info("从配置文件加载了 {} 个路由", config.getRoutes().size());
            } else {
                log.warn("配置文件中没有路由定义");
            }
            
        } catch (Exception e) {
            log.error("加载配置文件失败", e);
            throw new RuntimeException("加载路由配置失败", e);
        }
    }
    
    /**
     * 添加路由定义（支持动态添加）
     */
    public void addRouteDefinition(RouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            throw new IllegalArgumentException("路由定义不能为空");
        }
        
        try {
            // 应用全局配置合并
            RouteDefinition mergedDefinition = globalConfig.mergeRouteDefinition(routeDefinition);
            
            // 转换为路由对象（每次都创建新实例，确保隔离）
            EnhancedRoute route = configConverter.convertToRoute(mergedDefinition);
            
            // 添加到管理器
            addRoute(route);
            
            log.info("添加路由定义: {} ({})", routeDefinition.getId(), routeDefinition.getName());
            
        } catch (Exception e) {
            log.error("添加路由定义失败: {}", routeDefinition.getId(), e);
            throw new RuntimeException("添加路由定义失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新路由定义（重新创建整个Route对象）
     */
    public void updateRouteDefinition(String routeId, RouteDefinition routeDefinition) {
        if (routeId == null) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        if (routeDefinition == null) {
            throw new IllegalArgumentException("路由定义不能为空");
        }
        
        try {
            // 确保路由ID一致
            if (!routeId.equals(routeDefinition.getId())) {
                throw new IllegalArgumentException("路由ID不匹配: " + routeId + " vs " + routeDefinition.getId());
            }
            
            // 移除旧路由
            removeRoute(routeId);
            
            // 添加新路由（重新创建整个Route对象）
            addRouteDefinition(routeDefinition);
            
            log.info("更新路由定义: {} ({})", routeId, routeDefinition.getName());
            
        } catch (Exception e) {
            log.error("更新路由定义失败: {}", routeId, e);
            throw new RuntimeException("更新路由定义失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除路由定义
     */
    public void removeRouteDefinition(String routeId) {
        removeRoute(routeId);
        log.info("删除路由定义: {}", routeId);
    }
    
    /**
     * 批量添加路由定义
     */
    public void addRouteDefinitions(List<RouteDefinition> routeDefinitions) {
        if (routeDefinitions == null || routeDefinitions.isEmpty()) {
            return;
        }
        
        int successCount = 0;
        for (RouteDefinition definition : routeDefinitions) {
            try {
                addRouteDefinition(definition);
                successCount++;
            } catch (Exception e) {
                log.error("批量添加路由失败，跳过: {}", definition.getId(), e);
            }
        }
        
        log.info("批量添加路由完成，成功: {}, 失败: {}", successCount, routeDefinitions.size() - successCount);
    }
    
    /**
     * 添加路由
     */
    public void addRoute(EnhancedRoute route) {
        if (route == null) {
            throw new IllegalArgumentException("路由不能为空");
        }
        
        routes.put(route.getId(), route);
        updateSortedRoutes();
        
        log.debug("添加路由: {} ({})", route.getId(), route.getName());
    }
    
    /**
     * 删除路由
     */
    public void removeRoute(String routeId) {
        if (routeId == null) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        
        EnhancedRoute removed = routes.remove(routeId);
        if (removed != null) {
            updateSortedRoutes();
            log.debug("删除路由: {} ({})", removed.getId(), removed.getName());
        }
    }
    
    /**
     * 获取路由
     */
    public EnhancedRoute getRoute(String routeId) {
        return routes.get(routeId);
    }
    
    /**
     * 获取所有路由
     */
    public List<EnhancedRoute> getAllRoutes() {
        return new ArrayList<>(sortedRoutes);
    }
    
    /**
     * 获取启用的路由
     */
    public List<EnhancedRoute> getEnabledRoutes() {
        return sortedRoutes.stream()
                .filter(EnhancedRoute::isEnabled)
                .toList();
    }
    
    /**
     * 根据协议获取路由
     */
    public List<EnhancedRoute> getRoutesByProtocol(String protocolType) {
        return sortedRoutes.stream()
                .filter(route -> route.getInboundProtocol().getType().name().equalsIgnoreCase(protocolType))
                .toList();
    }
    
    /**
     * 根据服务名获取路由
     */
    public List<EnhancedRoute> getRoutesByService(String serviceName) {
        return sortedRoutes.stream()
                .filter(route -> serviceName.equals(route.getServiceName()))
                .toList();
    }
    
    /**
     * 启用路由
     */
    public void enableRoute(String routeId) {
        EnhancedRoute route = routes.get(routeId);
        if (route != null) {
            // 注意：EnhancedRoute是不可变的，需要重新创建
            EnhancedRoute enabledRoute = EnhancedRoute.builder()
                    .id(route.getId())
                    .name(route.getName())
                    .description(route.getDescription())
                    .order(route.getOrder())
                    .enabled(true)
                    .inboundProtocol(route.getInboundProtocol())
                    .predicates(route.getPredicates())
                    .filters(route.getFilters())
                    .target(route.getTarget())
                    .timeouts(route.getTimeouts())
                    .metadata(route.getMetadata())
                    .build();
            
            routes.put(routeId, enabledRoute);
            updateSortedRoutes();
            log.debug("启用路由: {}", routeId);
        }
    }
    
    /**
     * 禁用路由
     */
    public void disableRoute(String routeId) {
        EnhancedRoute route = routes.get(routeId);
        if (route != null) {
            // 注意：EnhancedRoute是不可变的，需要重新创建
            EnhancedRoute disabledRoute = EnhancedRoute.builder()
                    .id(route.getId())
                    .name(route.getName())
                    .description(route.getDescription())
                    .order(route.getOrder())
                    .enabled(false)
                    .inboundProtocol(route.getInboundProtocol())
                    .predicates(route.getPredicates())
                    .filters(route.getFilters())
                    .target(route.getTarget())
                    .timeouts(route.getTimeouts())
                    .metadata(route.getMetadata())
                    .build();
            
            routes.put(routeId, disabledRoute);
            updateSortedRoutes();
            log.debug("禁用路由: {}", routeId);
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfiguration() {
        log.info("重新加载路由配置...");
        
        try {
            // 清空现有路由
            routes.clear();
            
            // 重新加载配置
            loadConfiguration();
            
            log.info("路由配置重新加载完成，共 {} 个路由", routes.size());
            
        } catch (Exception e) {
            log.error("重新加载路由配置失败", e);
            throw new RuntimeException("重新加载路由配置失败", e);
        }
    }
    
    /**
     * 更新排序后的路由列表
     */
    private void updateSortedRoutes() {
        this.sortedRoutes = routes.values().stream()
                .sorted(Comparator.comparingInt(Route::getOrder))
                .toList();
    }
    
    /**
     * 选择目标节点（集成负载均衡功能）
     * 替代原LoadBalanceManager的selectTarget功能
     */
    public com.muxin.gateway.core.plus.route.node.EndpointAddress selectTarget(
            String routeId,
            java.util.List<com.muxin.gateway.core.plus.route.node.EndpointAddress> availableTargets,
            RequestContext context) {
        
        EnhancedRoute route = getRoute(routeId);
        if (route == null) {
            log.warn("路由不存在: {}", routeId);
            return null;
        }
        
        return configConverter.selectTarget(route.getTarget(), availableTargets, context);
    }
    
    /**
     * 根据路由选择目标节点
     */
    public com.muxin.gateway.core.plus.route.node.EndpointAddress selectTarget(
            EnhancedRoute route,
            java.util.List<com.muxin.gateway.core.plus.route.node.EndpointAddress> availableTargets,
            RequestContext context) {
        
        if (route == null) {
            return null;
        }
        
        return configConverter.selectTarget(route.getTarget(), availableTargets, context);
    }
    
    /**
     * 获取全局配置
     */
    public GlobalRouteConfig getGlobalConfig() {
        return globalConfig;
    }
    
    /**
     * 更新全局配置（会影响后续添加的路由）
     */
    public void updateGlobalConfig(GlobalRouteConfig newGlobalConfig) {
        // 注意：这里不会影响已存在的路由，只影响后续添加的路由
        // 如果需要应用到现有路由，需要调用 reapplyGlobalConfig()
        log.warn("全局配置已更新，如需应用到现有路由，请调用 reapplyGlobalConfig()");
    }
    
    /**
     * 重新应用全局配置到所有现有路由
     */
    public void reapplyGlobalConfig() {
        log.info("重新应用全局配置到 {} 个路由", routes.size());
        
        // 收集所有路由定义（需要从某处重新获取原始定义）
        // 这里简化处理，实际应该保存原始RouteDefinition
        log.warn("reapplyGlobalConfig功能需要保存原始RouteDefinition，当前版本暂不支持");
    }
    
    /**
     * 获取路由统计信息
     */
    public RouteStatistics getStatistics() {
        int totalRoutes = routes.size();
        long enabledRoutes = routes.values().stream()
                .mapToLong(route -> route.isEnabled() ? 1 : 0)
                .sum();
        
        long staticRoutes = routes.values().stream()
                .mapToLong(route -> route.isStaticTarget() ? 1 : 0)
                .sum();
        
        long discoveryRoutes = routes.values().stream()
                .mapToLong(route -> route.isDiscoveryTarget() ? 1 : 0)
                .sum();
        
        return RouteStatistics.builder()
                .totalRoutes(totalRoutes)
                .enabledRoutes((int) enabledRoutes)
                .disabledRoutes(totalRoutes - (int) enabledRoutes)
                .staticRoutes((int) staticRoutes)
                .discoveryRoutes((int) discoveryRoutes)
                .build();
    }
    
    /**
     * 路由统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class RouteStatistics {
        private int totalRoutes;
        private int enabledRoutes;
        private int disabledRoutes;
        private int staticRoutes;
        private int discoveryRoutes;
    }
} 