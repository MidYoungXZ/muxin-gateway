package com.muxin.gateway.admin.util;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.muxin.gateway.admin.context.DataScopeContext;
import com.muxin.gateway.admin.entity.SysDept;
import com.muxin.gateway.admin.entity.SysRole;
import com.muxin.gateway.admin.entity.SysUser;
import com.muxin.gateway.admin.mapper.DeptMapper;
import com.muxin.gateway.admin.mapper.RoleMapper;
import com.muxin.gateway.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.muxin.gateway.admin.entity.table.Tables.*;

/**
 * 数据权限工具类
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeHelper {
    
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    
    public DataScopeContext buildContext(Long userId) {
        DataScopeContext context = new DataScopeContext();
        context.setUserId(userId);
        
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            context.setDataScope(5);
            return context;
        }
        
        context.setDeptId(user.getDeptId());
        
        List<SysRole> roles = getActiveRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            context.setDataScope(5);
            return context;
        }
        
        int minDataScope = roles.stream()
                .map(r -> r.getDataScope() != null ? r.getDataScope() : 4)
                .min(Integer::compareTo)
                .orElse(4);
        
        context.setDataScope(minDataScope);
        
        if (minDataScope == 2) {
            Set<Long> allDeptIds = new HashSet<>();
            for (SysRole role : roles) {
                if (role.getDataScope() != null && role.getDataScope() == 2) {
                    List<Long> roleDeptIds = getRoleDeptIds(role.getId());
                    allDeptIds.addAll(roleDeptIds);
                }
            }
            context.setDeptIds(new ArrayList<>(allDeptIds));
        }
        
        if (minDataScope == 4 && user.getDeptId() != null) {
            List<Long> deptAndChildren = getDeptAndChildrenIds(user.getDeptId());
            context.setDeptAndChildrenIds(deptAndChildren);
        }
        
        return context;
    }
    
    public DataScopeContext getCurrentUserContext() {
        Long userId = StpUtil.getLoginIdAsLong();
        return buildContext(userId);
    }
    
    private List<SysRole> getActiveRolesByUserId(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .innerJoin(SYS_USER_ROLE).on(SYS_ROLE.ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .and(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1));
        
        return roleMapper.selectListByQuery(wrapper);
    }
    
    private List<Long> getRoleDeptIds(Long roleId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE_DEPT.DEPT_ID)
                .from(SYS_ROLE_DEPT)
                .where(SYS_ROLE_DEPT.ROLE_ID.eq(roleId));
        
        return roleMapper.selectObjectListByQuery(wrapper)
                .stream()
                .map(obj -> (Long) obj)
                .collect(Collectors.toList());
    }
    
    private List<Long> getDeptAndChildrenIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);
        
        collectChildDeptIds(deptId, result);
        
        return result;
    }
    
    private void collectChildDeptIds(Long parentId, List<Long> result) {
        List<SysDept> children = deptMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(SYS_DEPT)
                        .where(SYS_DEPT.PARENT_ID.eq(parentId))
                        .and(SYS_DEPT.DELETED.eq(0))
                        .and(SYS_DEPT.STATUS.eq(1))
        );
        
        for (SysDept child : children) {
            result.add(child.getId());
            collectChildDeptIds(child.getId(), result);
        }
    }
}