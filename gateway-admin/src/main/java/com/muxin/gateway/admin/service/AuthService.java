package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.LoginDTO;
import com.muxin.gateway.admin.model.vo.LoginVO;
import com.muxin.gateway.admin.model.vo.UserInfoVO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;

import java.util.List;

/**
 * 认证服务接口.
 * <p>
 * 该接口定义了用户认证相关的核心业务方法，包括：
 * <ul>
 *     <li>用户登录和登出</li>
 *     <li>用户信息查询</li>
 *     <li>权限和菜单管理</li>
 *     <li>令牌刷新</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录.
     * <p>
     * 验证用户凭据，成功后返回访问令牌和用户基本信息。
     * </p>
     *
     * @param dto 登录请求数据传输对象，包含用户名和密码
     * @return 登录响应对象，包含访问令牌和用户信息
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户登出.
     * <p>
     * 清除当前用户的登录状态和会话信息。
     * </p>
     */
    void logout();

    /**
     * 获取当前用户信息.
     * <p>
     * 根据当前请求的令牌获取已登录用户的详细信息。
     * </p>
     *
     * @return 用户信息视图对象
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 获取当前用户的菜单树.
     * <p>
     * 根据用户权限获取可访问的菜单树结构。
     * </p>
     *
     * @return 菜单树视图对象列表
     */
    List<MenuTreeVO> getCurrentUserMenus();

    /**
     * 获取指定用户的权限列表.
     * <p>
     * 查询用户拥有的所有权限码集合。
     * </p>
     *
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> getPermissions(Long userId);

    /**
     * 判断当前用户是否有指定权限.
     * <p>
     * 检查当前登录用户是否拥有指定的权限码。
     * </p>
     *
     * @param permission 权限码
     * @return true表示有权限，false表示无权限
     */
    boolean hasPermission(String permission);

    /**
     * 刷新访问令牌.
     * <p>
     * 当令牌即将过期时，刷新令牌以延长会话有效期。
     * </p>
     */
    void refreshToken();
} 