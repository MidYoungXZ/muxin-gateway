package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.MenuCreateDTO;
import com.muxin.gateway.admin.model.dto.MenuQueryDTO;
import com.muxin.gateway.admin.model.dto.MenuUpdateDTO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;
import com.muxin.gateway.admin.model.vo.MenuVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单管理相关接口")
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * 获取当前用户菜单树
     */
    @GetMapping("/user-tree")
    @Operation(summary = "获取当前用户菜单树", description = "获取当前登录用户的菜单树")
    public Result<List<MenuTreeVO>> getUserMenuTree() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(menuService.getUserMenuTreeVO(userId));
    }
    
    /**
     * 获取所有菜单树（用于权限分配）
     */
    @GetMapping("/tree")
    @Operation(summary = "获取所有菜单树", description = "获取系统所有菜单树结构")
    @SaCheckPermission("system:menu:list")
    public Result<List<MenuVO>> getAllMenuTree() {
        return Result.success(menuService.getAllMenuTree());
    }
    
    /**
     * 分页查询菜单列表
     */
    @GetMapping
    @Operation(summary = "分页查询菜单列表", description = "分页查询菜单列表")
    @SaCheckPermission("system:menu:list")
    public Result<PageVO<MenuVO>> listMenus(MenuQueryDTO query) {
        return Result.success(menuService.pageQuery(query));
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详情")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:view")
    public Result<MenuVO> getMenu(@PathVariable Long id) {
        return Result.success(menuService.getMenuDetail(id));
    }
    
    /**
     * 创建菜单
     */
    @PostMapping
    @Operation(summary = "创建菜单", description = "创建新菜单")
    @SaCheckPermission("system:menu:create")
    public Result<Long> createMenu(@RequestBody @Valid MenuCreateDTO dto) {
        return Result.success(menuService.createMenu(dto));
    }
    
    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单", description = "更新菜单信息")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:update")
    public Result<Void> updateMenu(@PathVariable Long id, 
                                   @RequestBody @Valid MenuUpdateDTO dto) {
        menuService.updateMenu(id, dto);
        return Result.success();
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "删除菜单")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:delete")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
    
    /**
     * 批量删除菜单
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单", description = "批量删除菜单")
    @SaCheckPermission("system:menu:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        menuService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 启用菜单
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用菜单", description = "启用菜单")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:update")
    public Result<Void> enableMenu(@PathVariable Long id) {
        menuService.enableMenu(id);
        return Result.success();
    }
    
    /**
     * 禁用菜单
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用菜单", description = "禁用菜单")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:update")
    public Result<Void> disableMenu(@PathVariable Long id) {
        menuService.disableMenu(id);
        return Result.success();
    }
    
    /**
     * 获取角色菜单ID列表
     */
    @GetMapping("/role/{roleId}/menu-ids")
    @Operation(summary = "获取角色菜单ID列表", description = "获取指定角色的菜单ID列表")
    @Parameter(name = "roleId", description = "角色ID", required = true)
    @SaCheckPermission("system:menu:view")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return Result.success(menuService.getMenuIdsByRoleId(roleId));
    }
    
    /**
     * 获取用户权限列表
     */
    @GetMapping("/user-permissions")
    @Operation(summary = "获取当前用户权限列表", description = "获取当前登录用户的权限列表")
    public Result<List<String>> getUserPermissions() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(menuService.getUserPermissions(userId));
    }
    
    /**
     * 移动菜单
     */
    @PutMapping("/{id}/move/{targetParentId}")
    @Operation(summary = "移动菜单", description = "移动菜单到新的父菜单下")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @Parameter(name = "targetParentId", description = "目标父菜单ID", required = true)
    @SaCheckPermission("system:menu:update")
    public Result<Void> moveMenu(@PathVariable Long id, @PathVariable Long targetParentId) {
        menuService.moveMenu(id, targetParentId);
        return Result.success();
    }
    
    /**
     * 调整菜单排序
     */
    @PutMapping("/{id}/sort")
    @Operation(summary = "调整菜单排序", description = "调整菜单排序")
    @Parameter(name = "id", description = "菜单ID", required = true)
    @SaCheckPermission("system:menu:update")
    public Result<Void> updateMenuSort(@PathVariable Long id, @RequestParam Integer sortOrder) {
        menuService.updateMenuSort(id, sortOrder);
        return Result.success();
    }
} 