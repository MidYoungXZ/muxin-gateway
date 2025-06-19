package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.RouteCreateDTO;
import com.muxin.gateway.admin.model.dto.RouteQueryDTO;
import com.muxin.gateway.admin.model.dto.RouteTestDTO;
import com.muxin.gateway.admin.model.dto.RouteUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteTestResultVO;
import com.muxin.gateway.admin.model.vo.RouteVO;
import com.muxin.gateway.admin.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 路由管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;
    
    /**
     * 分页查询路由列表
     */
    @GetMapping
    @SaCheckPermission("route:list")
    public Result<PageVO<RouteVO>> listRoutes(RouteQueryDTO query) {
        return Result.success(routeService.pageQuery(query));
    }
    
    /**
     * 获取路由详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("route:view")
    public Result<RouteVO> getRoute(@PathVariable Long id) {
        return Result.success(routeService.getRouteDetail(id));
    }
    
    /**
     * 创建路由
     */
    @PostMapping
    @SaCheckPermission("route:create")
    public Result<Long> createRoute(@RequestBody @Valid RouteCreateDTO dto) {
        return Result.success(routeService.createRoute(dto));
    }
    
    /**
     * 更新路由
     */
    @PutMapping("/{id}")
    @SaCheckPermission("route:update")
    public Result<Void> updateRoute(@PathVariable Long id, 
                                   @RequestBody @Valid RouteUpdateDTO dto) {
        routeService.updateRoute(id, dto);
        return Result.success();
    }
    
    /**
     * 删除路由
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:delete")
    public Result<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return Result.success();
    }
    
    /**
     * 启用路由
     */
    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:update")
    public Result<Void> enableRoute(@PathVariable Long id) {
        routeService.enableRoute(id);
        return Result.success();
    }
    
    /**
     * 禁用路由
     */
    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:update")
    public Result<Void> disableRoute(@PathVariable Long id) {
        routeService.disableRoute(id);
        return Result.success();
    }
    
    /**
     * 测试路由
     */
    @PostMapping("/test")
    @SaCheckPermission("route:test")
    public Result<RouteTestResultVO> testRoute(@RequestBody RouteTestDTO dto) {
        return Result.success(routeService.testRoute(dto));
    }
} 