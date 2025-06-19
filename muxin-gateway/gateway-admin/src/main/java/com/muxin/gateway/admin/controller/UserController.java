package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.PasswordUpdateDTO;
import com.muxin.gateway.admin.model.dto.UserCreateDTO;
import com.muxin.gateway.admin.model.dto.UserQueryDTO;
import com.muxin.gateway.admin.model.dto.UserUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.UserVO;
import com.muxin.gateway.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping
    @SaCheckPermission("system:user:list")
    public Result<PageVO<UserVO>> listUsers(UserQueryDTO query) {
        return Result.success(userService.pageQuery(query));
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:user:view")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userService.getUserDetail(userId));
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    @SaCheckPermission("system:user:create")
    public Result<Long> createUser(@RequestBody @Valid UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    public Result<Void> updateUser(@PathVariable Long id, 
                                   @RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("system:user:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    @SaCheckPermission("system:user:update")
    public Result<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success();
    }
    
    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    @SaCheckPermission("system:user:update")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @SaCheckPermission("system:user:update")
    public Result<Void> resetPassword(@PathVariable Long id, 
                                     @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable Long id, 
                                      @RequestBody @Valid PasswordUpdateDTO dto) {
        // 只能修改自己的密码
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!currentUserId.equals(id)) {
            return Result.error("只能修改自己的密码");
        }
        userService.updatePassword(id, dto);
        return Result.success();
    }
    
    /**
     * 分配角色
     */
    @PutMapping("/{id}/roles")
    @SaCheckPermission("system:user:update")
    public Result<Void> assignRoles(@PathVariable Long id, 
                                   @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
    
    /**
     * 获取用户的角色ID列表
     */
    @GetMapping("/{id}/role-ids")
    @SaCheckPermission("system:user:view")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }
} 