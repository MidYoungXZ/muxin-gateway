package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.LoginDTO;
import com.muxin.gateway.admin.model.vo.LoginVO;
import com.muxin.gateway.admin.model.vo.UserInfoVO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;

import java.util.List;

/**
 * 认证服务接口
 *
 * @author muxin
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUserInfo();
    
    /**
     * 获取当前用户菜单树
     */
    List<MenuTreeVO> getCurrentUserMenus();
    
    /**
     * 获取指定用户权限
     */
    List<String> getPermissions(Long userId);
    
    /**
     * 判断当前用户是否有指定权限
     */
    boolean hasPermission(String permission);
    
    /**
     * 刷新令牌
     */
    void refreshToken();
} 