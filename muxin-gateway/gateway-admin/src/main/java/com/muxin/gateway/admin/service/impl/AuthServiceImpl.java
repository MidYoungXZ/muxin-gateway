package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.model.dto.LoginDTO;
import com.muxin.gateway.admin.model.vo.LoginVO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;
import com.muxin.gateway.admin.model.vo.UserInfoVO;
import com.muxin.gateway.admin.model.vo.UserVO;
import com.muxin.gateway.admin.service.AuthService;
import com.muxin.gateway.admin.service.MenuService;
import com.muxin.gateway.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserService userService;
    private final MenuService menuService;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        log.info("用户登录请求 - 用户名: {}", dto.getUsername());
        
        // 根据用户名查询用户
        UserVO user = userService.getByUsername(dto.getUsername());
        log.info("数据库查询结果 - 用户存在: {}", user != null);
        
        if (user == null) {
            log.warn("登录失败 - 用户不存在: {}", dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        log.info("用户信息 - ID: {}, 用户名: {}, 状态: {}", user.getId(), user.getUsername(), user.getStatus());
        log.info("密码验证 - 输入密码: {}, 数据库密码hash: {}", dto.getPassword(), user.getPassword());
        
        // 验证密码
        boolean passwordValid = BCrypt.checkpw(dto.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", passwordValid);
        
        if (!passwordValid) {
            log.warn("登录失败 - 密码错误: {}", dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 登录
        StpUtil.login(user.getId());
        
        // 用户信息
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        
        // 设置权限标识
        List<String> permissions = getPermissions(user.getId());
        userInfo.setPermissions(permissions);
        
        // 设置角色编码
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            List<String> roleCodes = user.getRoles().stream()
                    .map(role -> role.getRoleCode())
                    .collect(Collectors.toList());
            userInfo.setRoles(roleCodes);
        }
        
        // 构建登录返回信息
        LoginVO loginVO = LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .accessToken(StpUtil.getTokenValue())
                .tokenType("Bearer")
                .expireTime(StpUtil.getTokenTimeout())
                .expiresIn(StpUtil.getTokenTimeout())
                .userInfo(userInfo)
                .build();
        
        log.info("用户登录成功：{}", user.getUsername());
        return loginVO;
    }
    
    @Override
    public void logout() {
        String username = getCurrentUsername();
        StpUtil.logout();
        log.info("用户退出登录：{}", username);
    }
    
    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserVO user = userService.getUserDetail(userId);
        
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        
        // 设置权限标识
        List<String> permissions = getPermissions(userId);
        userInfo.setPermissions(permissions);
        
        // 设置角色编码
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            List<String> roleCodes = user.getRoles().stream()
                    .map(role -> role.getRoleCode())
                    .collect(Collectors.toList());
            userInfo.setRoles(roleCodes);
        }
        
        return userInfo;
    }
    
    @Override
    public List<MenuTreeVO> getCurrentUserMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        return menuService.getUserMenuTreeVO(userId);
    }
    
    @Override
    public List<String> getPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 获取用户权限
        return menuService.getUserPermissions(userId);
    }
    
    @Override
    public boolean hasPermission(String permission) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> permissions = getPermissions(userId);
        return permissions.contains(permission);
    }
    
    @Override
    public void refreshToken() {
        StpUtil.renewTimeout(StpUtil.getTokenTimeout());
        log.info("刷新用户Token：{}", getCurrentUsername());
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            UserVO user = userService.getUserDetail(userId);
            return user != null ? user.getUsername() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
} 