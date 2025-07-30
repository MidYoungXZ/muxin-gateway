package com.muxin.gateway.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 *
 * @author muxin
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
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
     * 返回一个账号所拥有的角色标识集合
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