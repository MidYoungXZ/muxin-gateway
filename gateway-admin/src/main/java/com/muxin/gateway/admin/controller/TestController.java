package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.secure.BCrypt;
import com.muxin.gateway.admin.entity.SysUser;
import com.muxin.gateway.admin.mapper.UserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 测试控制器 - 仅用于开发环境
 *
 * ⚠️ 警告：此控制器绕过了所有认证，绝对不能部署到生产环境！
 * ⚠️ WARNING: This controller bypasses all authentication, MUST NOT be deployed to production!
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@SaIgnore  // 忽略认证
@Profile("dev")  // 仅在开发环境激活，禁止部署到生产环境
public class TestController {
    
    private final UserMapper userMapper;
    
    /**
     * 初始化测试用户
     */
    @PostMapping("/init-user")
    public String initTestUser() {
        try {
            // 检查admin用户是否存在
            SysUser existingUser = userMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                    .eq("username", "admin")
            );
            
            if (existingUser != null) {
                return "Admin user already exists!";
            }
            
            // 创建admin用户
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(BCrypt.hashpw("admin123"));
            admin.setNickname("超级管理员");
            admin.setEmail("admin@muxin.com");
            admin.setMobile("13800138000");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            
            userMapper.insert(admin);
            
            return "Test user created successfully! Username: admin, Password: admin123";
        } catch (Exception e) {
            return "Error creating test user: " + e.getMessage();
        }
    }
    
    /**
     * 检查用户是否存在
     */
    @GetMapping("/check-user/{username}")
    public String checkUser(@PathVariable String username) {
        SysUser user = userMapper.selectOneByQuery(
            QueryWrapper.create()
                .eq("username", username)
        );
        
        if (user != null) {
            return "User exists: " + user.getUsername() + " (ID: " + user.getId() + ")";
        } else {
            return "User not found: " + username;
        }
    }

    /**
     * 重置admin用户密码
     */
    @PostMapping("/reset-admin-password")
    public String resetAdminPassword() {
        try {
            SysUser user = userMapper.selectOneByQuery(
                QueryWrapper.create().eq("username", "admin")
            );
            
            if (user == null) {
                return "Admin user not found!";
            }
            
            user.setPassword(BCrypt.hashpw("admin123"));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.update(user);
            
            return "Admin password reset successfully! Password: admin123";
        } catch (Exception e) {
            return "Error resetting password: " + e.getMessage();
        }
    }
} 