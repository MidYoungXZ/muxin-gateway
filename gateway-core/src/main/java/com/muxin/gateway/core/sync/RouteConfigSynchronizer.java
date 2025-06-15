package com.muxin.gateway.core.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.muxin.gateway.core.entity.GatewayRoute;
import com.muxin.gateway.core.mapper.GatewayRouteMapper;
import com.muxin.gateway.core.route.RouteDefinition;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import com.muxin.gateway.core.filter.FilterDefinition;
import com.muxin.gateway.core.route.PredicateDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 路由配置同步器
 * 定期从数据库读取路由配置并更新到内存中
 * 
 * @author muxin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteConfigSynchronizer {
    
    private final GatewayRouteMapper routeMapper;
    private final RouteDefinitionRepository routeDefinitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 当前版本号
     */
    private final AtomicInteger currentVersion = new AtomicInteger(0);
    
    /**
     * 读写锁，防止并发修改
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * 初始化时加载路由配置
     */
    @PostConstruct
    public void init() {
        syncRoutes();
    }
    
    /**
     * 定期同步路由配置（30秒执行一次）
     */
    @Scheduled(fixedDelay = 30000)
    public void syncRoutes() {
        // 使用写锁，确保同步过程中不会有其他操作干扰
        lock.writeLock().lock();
        try {
            // 获取数据库中最大版本号
            Integer maxVersion = routeMapper.getMaxVersion();
            if (maxVersion == null) {
                maxVersion = 0;
            }
            
            // 如果版本号没有变化，不需要更新
            if (maxVersion <= currentVersion.get()) {
                return;
            }
            
            log.info("Detected route configuration change, version: {} -> {}", currentVersion.get(), maxVersion);
            
            // 查询所有启用的路由
            QueryWrapper<GatewayRoute> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enabled", true)
                       .orderByAsc("order_num");

            List<GatewayRoute> routes = routeMapper.selectList(queryWrapper);
            
            // 转换为RouteDefinition
            List<RouteDefinition> routeDefinitions = routes.stream()
                    .map(this::convertToRouteDefinition)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 安全地清空现有路由：先获取所有路由ID，然后逐个删除
            List<String> existingRouteIds = routeDefinitionRepository.findAll()
                    .stream()
                    .map(RouteDefinition::getId)
                    .collect(Collectors.toList());
            
            // 逐个删除现有路由，避免并发修改异常
            for (String routeId : existingRouteIds) {
                try {
                    routeDefinitionRepository.deleteById(routeId);
                } catch (Exception e) {
                    log.warn("Failed to delete route: {}, error: {}", routeId, e.getMessage());
                }
            }
            
            // 添加新路由
            for (RouteDefinition routeDefinition : routeDefinitions) {
                try {
                    routeDefinitionRepository.save(routeDefinition);
                } catch (Exception e) {
                    log.error("Failed to save route: {}, error: {}", routeDefinition.getId(), e.getMessage());
                }
            }
            
            // 更新版本号
            currentVersion.set(maxVersion);
            
            log.info("Route configuration sync completed, loaded {} routes", routeDefinitions.size());
            
        } catch (Exception e) {
            log.error("Failed to sync route configuration", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取同步状态
     */
    public Map<String, Object> getSyncStatus() {
        lock.readLock().lock();
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("currentVersion", currentVersion.get());
            status.put("totalRoutes", routeDefinitionRepository.findAll().size());
            status.put("lastSyncTime", System.currentTimeMillis());
            return status;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 手动触发同步
     */
    public void manualSync() {
        log.info("Manual sync triggered");
        syncRoutes();
    }
    
    /**
     * 将数据库实体转换为路由定义
     */
    private RouteDefinition convertToRouteDefinition(GatewayRoute route) {
        try {
            RouteDefinition definition = new RouteDefinition();
            definition.setId(route.getId());
            definition.setUri(URI.create(route.getUri()));
            definition.setOrder(route.getOrderNum());
            
            // 解析predicates
            if (StringUtils.hasText(route.getPredicates())) {
                List<PredicateDefinition> predicates = objectMapper.readValue(
                    route.getPredicates(), 
                    new TypeReference<List<PredicateDefinition>>() {}
                );
                definition.setPredicates(predicates);
            }
            
            // 解析filters
            if (StringUtils.hasText(route.getFilters())) {
                List<FilterDefinition> filters = objectMapper.readValue(
                    route.getFilters(), 
                    new TypeReference<List<FilterDefinition>>() {}
                );
                definition.setFilters(filters);
            }
            
            // 解析metadata
            if (StringUtils.hasText(route.getMetadata())) {
                Map<String, Object> metadata = objectMapper.readValue(
                    route.getMetadata(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                definition.setMetadata(metadata);
            }
            
            return definition;
            
        } catch (Exception e) {
            log.error("Failed to convert route: {}", route.getId(), e);
            return null;
        }
    }
} 