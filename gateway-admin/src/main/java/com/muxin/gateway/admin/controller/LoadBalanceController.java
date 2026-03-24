package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.LoadBalanceCreateDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceQueryDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceUpdateDTO;
import com.muxin.gateway.admin.model.vo.LoadBalanceStrategyVO;
import com.muxin.gateway.admin.model.vo.LoadBalanceVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.LoadBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 负载均衡管理控制器
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/load-balance")
@RequiredArgsConstructor
public class LoadBalanceController {
    
    private final LoadBalanceService loadBalanceService;
    
    @GetMapping
    @SaCheckPermission("route:list")
    public Result<PageVO<LoadBalanceVO>> list(LoadBalanceQueryDTO query) {
        return Result.success(loadBalanceService.pageQuery(query));
    }
    
    @GetMapping("/route/{routeId}")
    @SaCheckPermission("route:list")
    public Result<LoadBalanceVO> getByRouteId(@PathVariable Long routeId) {
        return Result.success(loadBalanceService.getByRouteId(routeId));
    }
    
    @GetMapping("/{id}")
    @SaCheckPermission("route:list")
    public Result<LoadBalanceVO> getDetail(@PathVariable Long id) {
        return Result.success(loadBalanceService.getDetail(id));
    }
    
    @PostMapping
    @SaCheckPermission("route:update")
    public Result<Long> create(@RequestBody @Valid LoadBalanceCreateDTO dto) {
        return Result.success(loadBalanceService.create(dto));
    }
    
    @PutMapping("/{id}")
    @SaCheckPermission("route:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid LoadBalanceUpdateDTO dto) {
        loadBalanceService.update(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:update")
    public Result<Void> delete(@PathVariable Long id) {
        loadBalanceService.delete(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:update")
    public Result<Void> enable(@PathVariable Long id) {
        loadBalanceService.enable(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:update")
    public Result<Void> disable(@PathVariable Long id) {
        loadBalanceService.disable(id);
        return Result.success();
    }
    
    @GetMapping("/strategies")
    public Result<List<LoadBalanceStrategyVO>> getStrategies() {
        return Result.success(loadBalanceService.getStrategies());
    }
}