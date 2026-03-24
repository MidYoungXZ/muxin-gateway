package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.ServiceCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.ServiceNodeVO;
import com.muxin.gateway.admin.model.vo.ServiceStatsVO;
import com.muxin.gateway.admin.service.ServiceNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 服务节点管理控制器
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class ServiceNodeController {
    
    private final ServiceNodeService serviceNodeService;
    
    @GetMapping("/services")
    @SaCheckPermission("route:node:list")
    public Result<List<ServiceStatsVO>> getServiceStats(
            @RequestParam(required = false) String serviceName) {
        return Result.success(serviceNodeService.getServiceStats(serviceName));
    }
    
    @GetMapping("/services/{serviceName}/nodes")
    @SaCheckPermission("route:node:list")
    public Result<PageVO<ServiceNodeVO>> getNodesByService(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(serviceNodeService.getNodesByService(serviceName, pageNum, pageSize));
    }
    
    @GetMapping("/{id}")
    @SaCheckPermission("route:node:list")
    public Result<ServiceNodeVO> getDetail(@PathVariable Long id) {
        return Result.success(serviceNodeService.getDetail(id));
    }
    
    @PostMapping
    @SaCheckPermission("route:node:create")
    public Result<Long> create(@RequestBody @Valid ServiceNodeCreateDTO dto) {
        return Result.success(serviceNodeService.create(dto));
    }
    
    @PutMapping("/{id}")
    @SaCheckPermission("route:node:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ServiceNodeUpdateDTO dto) {
        serviceNodeService.update(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:node:delete")
    public Result<Void> delete(@PathVariable Long id) {
        serviceNodeService.delete(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:node:update")
    public Result<Void> enable(@PathVariable Long id) {
        serviceNodeService.enable(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:node:update")
    public Result<Void> disable(@PathVariable Long id) {
        serviceNodeService.disable(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/maintenance")
    @SaCheckPermission("route:node:update")
    public Result<Void> maintenance(@PathVariable Long id) {
        serviceNodeService.maintenance(id);
        return Result.success();
    }
    
    @GetMapping("/service-names")
    public Result<List<String>> getServiceNames() {
        return Result.success(serviceNodeService.getServiceNames());
    }
    
    @PostMapping("/services")
    @SaCheckPermission("route:node:create")
    public Result<Long> createService(@RequestBody @Valid ServiceCreateDTO dto) {
        return Result.success(serviceNodeService.createService(dto));
    }
}