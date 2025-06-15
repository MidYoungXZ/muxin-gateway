package com.muxin.gateway.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.muxin.gateway.admin.model.ApiResponse;
import com.muxin.gateway.core.entity.GatewayRoute;
import com.muxin.gateway.core.mapper.GatewayRouteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

/**
 * 路由管理控制器
 * 
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/routes")
@RequiredArgsConstructor
public class RouteController {
    
    private final GatewayRouteMapper routeMapper;
    private final AtomicInteger versionCounter = new AtomicInteger(1);
    
    /**
     * 获取所有路由列表
     */
    @GetMapping
    public ApiResponse<List<GatewayRoute>> listRoutes() {
        QueryWrapper<GatewayRoute> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("order_num");
        List<GatewayRoute> routes = routeMapper.selectList(queryWrapper);
        return ApiResponse.success(routes);
    }
    
    /**
     * 获取单个路由详情
     */
    @GetMapping("/{id}")
    public ApiResponse<GatewayRoute> getRoute(@PathVariable String id) {
        GatewayRoute route = routeMapper.selectById(id);
        if (route == null) {
            return ApiResponse.error(404, "路由不存在");
        }
        return ApiResponse.success(route);
    }
    
    /**
     * 创建新路由
     */
    @PostMapping
    public ApiResponse<GatewayRoute> createRoute(@RequestBody GatewayRoute route) {
        // 检查路由ID是否已存在
        if (routeMapper.selectById(route.getId()) != null) {
            return ApiResponse.error(400, "路由ID已存在");
        }
        
        route.setCreatedTime(LocalDateTime.now());
        route.setUpdatedTime(LocalDateTime.now());
        route.setVersion(versionCounter.incrementAndGet());
        if (route.getEnabled() == null) {
            route.setEnabled(true);
        }
        if (route.getOrderNum() == null) {
            route.setOrderNum(0);
        }
        routeMapper.insert(route);
        log.info("Created route: {}", route.getId());
        return ApiResponse.success("路由创建成功", route);
    }
    
    /**
     * 更新路由
     */
    @PutMapping("/{id}")
    public ApiResponse<GatewayRoute> updateRoute(@PathVariable String id, @RequestBody GatewayRoute route) {
        GatewayRoute existingRoute = routeMapper.selectById(id);
        if (existingRoute == null) {
            return ApiResponse.error(404, "路由不存在");
        }
        
        route.setId(id);
        route.setUpdatedTime(LocalDateTime.now());
        route.setVersion(versionCounter.incrementAndGet());
        route.setCreatedTime(existingRoute.getCreatedTime()); // 保留创建时间
        routeMapper.updateById(route);
        log.info("Updated route: {}", id);
        return ApiResponse.success("路由更新成功", route);
    }
    
    /**
     * 删除路由
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoute(@PathVariable String id) {
        int deleted = routeMapper.deleteById(id);
        if (deleted == 0) {
            return ApiResponse.error(404, "路由不存在");
        }
        log.info("Deleted route: {}", id);
        return ApiResponse.success("路由删除成功", null);
    }
    
    /**
     * 启用路由
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<Void> enableRoute(@PathVariable String id) {
        GatewayRoute route = routeMapper.selectById(id);
        if (route == null) {
            return ApiResponse.error(404, "路由不存在");
        }
        
        route.setEnabled(true);
        route.setUpdatedTime(LocalDateTime.now());
        route.setVersion(versionCounter.incrementAndGet());
        routeMapper.updateById(route);
        log.info("Enabled route: {}", id);
        return ApiResponse.success("路由启用成功", null);
    }
    
    /**
     * 禁用路由
     */
    @PutMapping("/{id}/disable")
    public ApiResponse<Void> disableRoute(@PathVariable String id) {
        GatewayRoute route = routeMapper.selectById(id);
        if (route == null) {
            return ApiResponse.error(404, "路由不存在");
        }
        
        route.setEnabled(false);
        route.setUpdatedTime(LocalDateTime.now());
        route.setVersion(versionCounter.incrementAndGet());
        routeMapper.updateById(route);
        log.info("Disabled route: {}", id);
        return ApiResponse.success("路由禁用成功", null);
    }
    
    /**
     * 导出路由配置
     */
    @GetMapping("/export")
    public ApiResponse<List<GatewayRoute>> exportRoutes() {
        QueryWrapper<GatewayRoute> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("order_num");
        List<GatewayRoute> routes = routeMapper.selectList(queryWrapper);
        log.info("Exported {} routes", routes.size());
        return ApiResponse.success(routes);
    }
    
    /**
     * 导入路由配置
     */
    @PostMapping("/import")
    public ApiResponse<Void> importRoutes(@RequestBody Map<String, Object> importData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> routeDataList = (List<Map<String, Object>>) importData.get("data");
            
            if (routeDataList == null || routeDataList.isEmpty()) {
                return ApiResponse.error(400, "导入数据为空");
            }
            
            int importCount = 0;
            for (Map<String, Object> routeData : routeDataList) {
                GatewayRoute route = new GatewayRoute();
                route.setId((String) routeData.get("id"));
                route.setUri((String) routeData.get("uri"));
                route.setPredicates((String) routeData.get("predicates"));
                route.setFilters((String) routeData.get("filters"));
                route.setMetadata((String) routeData.get("metadata"));
                route.setOrderNum((Integer) routeData.get("orderNum"));
                route.setEnabled((Boolean) routeData.get("enabled"));
                
                // 检查是否已存在
                GatewayRoute existing = routeMapper.selectById(route.getId());
                if (existing != null) {
                    // 更新现有路由
                    route.setCreatedTime(existing.getCreatedTime());
                    route.setUpdatedTime(LocalDateTime.now());
                    route.setVersion(versionCounter.incrementAndGet());
                    routeMapper.updateById(route);
                } else {
                    // 创建新路由
                    route.setCreatedTime(LocalDateTime.now());
                    route.setUpdatedTime(LocalDateTime.now());
                    route.setVersion(versionCounter.incrementAndGet());
                    routeMapper.insert(route);
                }
                importCount++;
            }
            
            log.info("Imported {} routes", importCount);
            return ApiResponse.success("成功导入 " + importCount + " 个路由配置", null);
        } catch (Exception e) {
            log.error("Failed to import routes", e);
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
    }
} 