package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.ConfigCreateDTO;
import com.muxin.gateway.admin.model.dto.ConfigQueryDTO;
import com.muxin.gateway.admin.model.dto.ConfigUpdateDTO;
import com.muxin.gateway.admin.model.vo.ConfigVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Tag(name = "系统配置", description = "系统配置管理接口")
public class ConfigController {
    
    private final ConfigService configService;
    
    @GetMapping
    @Operation(summary = "分页查询配置", description = "分页查询系统配置列表")
    @SaCheckPermission("system:config:list")
    public Result<PageVO<ConfigVO>> list(ConfigQueryDTO query) {
        return Result.success(configService.pageQuery(query));
    }
    
    @GetMapping("/all")
    @Operation(summary = "获取所有配置", description = "获取所有启用的配置")
    public Result<List<ConfigVO>> listAll() {
        return Result.success(configService.getAllConfigs());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情", description = "根据ID获取配置详情")
    @Parameter(name = "id", description = "配置ID", required = true)
    @SaCheckPermission("system:config:view")
    public Result<ConfigVO> getDetail(@PathVariable Long id) {
        return Result.success(configService.getDetail(id));
    }
    
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据键获取配置", description = "根据配置键获取配置值")
    @Parameter(name = "configKey", description = "配置键", required = true)
    public Result<ConfigVO> getByKey(@PathVariable String configKey) {
        return Result.success(configService.getByKey(configKey));
    }
    
    @PostMapping
    @Operation(summary = "创建配置", description = "创建新配置")
    @SaCheckPermission("system:config:create")
    public Result<Long> create(@RequestBody @Valid ConfigCreateDTO dto) {
        return Result.success(configService.create(dto));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新配置", description = "更新配置信息")
    @Parameter(name = "id", description = "配置ID", required = true)
    @SaCheckPermission("system:config:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ConfigUpdateDTO dto) {
        configService.update(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置", description = "删除配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @SaCheckPermission("system:config:delete")
    public Result<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除配置", description = "批量删除配置")
    @SaCheckPermission("system:config:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        configService.batchDelete(ids);
        return Result.success();
    }
    
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用配置", description = "启用配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @SaCheckPermission("system:config:update")
    public Result<Void> enable(@PathVariable Long id) {
        configService.enable(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用配置", description = "禁用配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @SaCheckPermission("system:config:update")
    public Result<Void> disable(@PathVariable Long id) {
        configService.disable(id);
        return Result.success();
    }
    
    @GetMapping("/check-key")
    @Operation(summary = "检查配置键", description = "检查配置键是否可用")
    public Result<Boolean> checkKey(@RequestParam String configKey,
                                    @RequestParam(required = false) Long excludeId) {
        return Result.success(configService.checkKeyAvailable(configKey, excludeId));
    }
    
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新缓存", description = "刷新配置缓存")
    @SaCheckPermission("system:config:update")
    public Result<Void> refreshCache() {
        configService.refreshCache();
        return Result.success();
    }
}