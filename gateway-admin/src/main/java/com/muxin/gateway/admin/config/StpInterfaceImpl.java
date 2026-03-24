package com.muxin.gateway.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import com.muxin.gateway.admin.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionMapper permissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            Long userId = Long.parseLong(loginId.toString());
            List<String> permissions = permissionMapper.selectPermissionsByUserId(userId);
            if (log.isDebugEnabled()) {
                log.debug("User {} has permissions: {}", userId, permissions);
            }
            return permissions != null ? permissions : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to get permissions for user: {}", loginId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Long userId = Long.parseLong(loginId.toString());
            List<String> roles = permissionMapper.selectRolesByUserId(userId);
            if (log.isDebugEnabled()) {
                log.debug("User {} has roles: {}", userId, roles);
            }
            return roles != null ? roles : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to get roles for user: {}", loginId, e);
            return new ArrayList<>();
        }
    }
} 