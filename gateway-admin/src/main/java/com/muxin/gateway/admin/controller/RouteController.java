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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;
    
    @GetMapping
    @SaCheckPermission("route:list")
    public Result<PageVO<RouteVO>> listRoutes(RouteQueryDTO query) {
        return Result.success(routeService.pageQuery(query));
    }
    
    @GetMapping("/{id}")
    @SaCheckPermission("route:view")
    public Result<RouteVO> getRoute(@PathVariable Long id) {
        return Result.success(routeService.getRouteDetail(id));
    }
    
    @PostMapping
    @SaCheckPermission("route:create")
    public Result<Long> createRoute(@RequestBody @Valid RouteCreateDTO dto) {
        return Result.success(routeService.createRoute(dto));
    }
    
    @PutMapping("/{id}")
    @SaCheckPermission("route:update")
    public Result<Void> updateRoute(@PathVariable Long id, 
                                   @RequestBody @Valid RouteUpdateDTO dto) {
        routeService.updateRoute(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:delete")
    public Result<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @SaCheckPermission("route:delete")
    public Result<Void> batchDeleteRoutes(@RequestBody List<Long> ids) {
        routeService.batchDelete(ids);
        return Result.success();
    }
    
    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:update")
    public Result<Void> enableRoute(@PathVariable Long id) {
        routeService.enableRoute(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:update")
    public Result<Void> disableRoute(@PathVariable Long id) {
        routeService.disableRoute(id);
        return Result.success();
    }
    
    @PostMapping("/test")
    @SaCheckPermission("route:test")
    public Result<RouteTestResultVO> testRoute(@RequestBody RouteTestDTO dto) {
        return Result.success(routeService.testRoute(dto));
    }
    
    @GetMapping("/services")
    @SaCheckPermission("route:list")
    public Result<List<String>> getServiceNames() {
        return Result.success(routeService.getServiceNames());
    }
} 