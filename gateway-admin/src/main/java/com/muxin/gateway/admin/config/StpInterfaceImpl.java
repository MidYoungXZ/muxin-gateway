package com.muxin.gateway.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token权限认证接口实现类.
 * <p>
 * 该类实现了Sa-Token的StpInterface接口，用于提供用户权限和角色信息。
 * 主要功能包括：
 * <ul>
 *     <li>根据登录ID获取用户的权限码集合</li>
 *     <li>根据登录ID获取用户的角色标识集合</li>
 * </ul>
 * </p>
 * <p>
 * 注意：当前实现返回所有权限和管理员角色，生产环境需要根据实际业务逻辑实现。
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回指定账号所拥有的权限码集合.
     * <p>
     * 该方法用于权限认证，Sa-Token会根据返回的权限码集合进行权限验证。
     * </p>
     * <p>
     * 注意：当前实现返回所有权限（*），生产环境需要根据loginId查询数据库获取实际权限。
     * </p>
     *
     * @param loginId 账号id，即用户ID
     * @param loginType 账号类型，可用于多账号体系
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // TODO: 实现权限查询逻辑
        List<String> permissions = new ArrayList<>();
        // 暂时返回所有权限
        permissions.add("*");
        return permissions;
    }

    /**
     * 返回指定账号所拥有的角色标识集合.
     * <p>
     * 该方法用于角色认证，Sa-Token会根据返回的角色集合进行角色验证。
     * </p>
     * <p>
     * 注意：当前实现返回管理员角色，生产环境需要根据loginId查询数据库获取实际角色。
     * </p>
     *
     * @param loginId 账号id，即用户ID
     * @param loginType 账号类型，可用于多账号体系
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO: 实现角色查询逻辑
        List<String> roles = new ArrayList<>();
        // 暂时返回管理员角色
        roles.add("admin");
        return roles;
    }
} 