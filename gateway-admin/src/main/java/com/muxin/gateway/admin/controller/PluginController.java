package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.entity.GwPlugin;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.PluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 插件管理Controller
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/plugins")
@RequiredArgsConstructor
public class PluginController {
    
    private final PluginService pluginService;
    
    @GetMapping
    @SaCheckPermission("route:plugin:list")
    public Result<PageVO<GwPlugin>> listPlugins(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String pluginName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(pluginService.getPluginsByType(type, pluginName, pageNum, pageSize));
    }
    
    @GetMapping("/{id}")
    @SaCheckPermission("route:plugin:view")
    public Result<GwPlugin> getPlugin(@PathVariable Long id) {
        return Result.success(pluginService.getPluginById(id));
    }
    
    @PostMapping
    @SaCheckPermission("route:plugin:create")
    public Result<Long> createPlugin(@RequestBody GwPlugin plugin) {
        return Result.success(pluginService.createPlugin(plugin));
    }
    
    @PutMapping("/{id}")
    @SaCheckPermission("route:plugin:update")
    public Result<Void> updatePlugin(@PathVariable Long id, @RequestBody GwPlugin plugin) {
        plugin.setId(id);
        pluginService.updatePlugin(plugin);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:plugin:delete")
    public Result<Void> deletePlugin(@PathVariable Long id) {
        pluginService.deletePlugin(id);
        return Result.success();
    }
}