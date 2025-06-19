package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.RoleCreateDTO;
import com.muxin.gateway.admin.model.dto.RoleQueryDTO;
import com.muxin.gateway.admin.model.dto.RoleUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RoleVO;
import com.muxin.gateway.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 分页查询角色列表
     */
    @GetMapping
    @SaCheckPermission("system:role:list")
    public Result<PageVO<RoleVO>> listRoles(RoleQueryDTO query) {
        return Result.success(roleService.pageQuery(query));
    }
    
    /**
     * 获取所有角色列表
     */
    @GetMapping("/all")
    @SaCheckPermission("system:role:list")
    public Result<List<RoleVO>> listAllRoles() {
        return Result.success(roleService.listAll());
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:role:view")
    public Result<RoleVO> getRole(@PathVariable Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    @SaCheckPermission("system:role:create")
    public Result<Long> createRole(@RequestBody @Valid RoleCreateDTO dto) {
        return Result.success(roleService.createRole(dto));
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:update")
    public Result<Void> updateRole(@PathVariable Long id, 
                                   @RequestBody @Valid RoleUpdateDTO dto) {
        roleService.updateRole(id, dto);
        return Result.success();
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
    
    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("system:role:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        roleService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 启用角色
     */
    @PostMapping("/{id}/enable")
    @SaCheckPermission("system:role:update")
    public Result<Void> enableRole(@PathVariable Long id) {
        roleService.enableRole(id);
        return Result.success();
    }
    
    /**
     * 禁用角色
     */
    @PostMapping("/{id}/disable")
    @SaCheckPermission("system:role:update")
    public Result<Void> disableRole(@PathVariable Long id) {
        roleService.disableRole(id);
        return Result.success();
    }
    
    /**
     * 分配菜单权限
     */
    @PutMapping("/{id}/menus")
    @SaCheckPermission("system:role:update")
    public Result<Void> assignMenus(@PathVariable Long id, 
                                   @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
    
    /**
     * 获取角色的菜单ID列表
     */
    @GetMapping("/{id}/menu-ids")
    @SaCheckPermission("system:role:view")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.success(roleService.getRoleMenuIds(id));
    }
    
    /**
     * 检查角色编码是否可用
     */
    @GetMapping("/check-code")
    public Result<Boolean> checkRoleCode(@RequestParam String roleCode, 
                                        @RequestParam(required = false) Long excludeId) {
        return Result.success(roleService.checkRoleCodeAvailable(roleCode, excludeId));
    }
    
    /**
     * 获取角色统计信息
     */
    @GetMapping("/stats")
    @SaCheckPermission("system:role:list")
    public Result<Object> getRoleStats() {
        return Result.success(roleService.getRoleStats());
    }
} 