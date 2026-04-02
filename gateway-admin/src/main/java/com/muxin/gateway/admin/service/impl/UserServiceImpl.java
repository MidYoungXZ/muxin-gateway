package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.annotation.DataScope;
import com.muxin.gateway.admin.context.DataScopeContext;
import com.muxin.gateway.admin.entity.SysDept;
import com.muxin.gateway.admin.entity.SysRole;
import com.muxin.gateway.admin.entity.SysUser;
import com.muxin.gateway.admin.entity.SysUserRole;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.DeptMapper;
import com.muxin.gateway.admin.mapper.RoleMapper;
import com.muxin.gateway.admin.mapper.UserMapper;
import com.muxin.gateway.admin.mapper.UserRoleMapper;
import com.muxin.gateway.admin.model.dto.PasswordUpdateDTO;
import com.muxin.gateway.admin.model.dto.ProfileUpdateDTO;
import com.muxin.gateway.admin.model.dto.UserCreateDTO;
import com.muxin.gateway.admin.model.dto.UserQueryDTO;
import com.muxin.gateway.admin.model.dto.UserUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RoleVO;
import com.muxin.gateway.admin.model.vo.UserVO;
import com.muxin.gateway.admin.service.RoleService;
import com.muxin.gateway.admin.service.UserService;
import com.muxin.gateway.admin.util.DataScopeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {
    
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final RoleService roleService;
    private final DataScopeHelper dataScopeHelper;
    
    @Override
    public PageVO<UserVO> pageQuery(UserQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(SYS_USER)
                .where(SYS_USER.DELETED.eq(0))
                .and(SYS_USER.USERNAME.like(query.getUsername() != null ? "%" + query.getUsername() + "%" : null, query.getUsername() != null))
                .and(SYS_USER.NICKNAME.like(query.getNickname() != null ? "%" + query.getNickname() + "%" : null, query.getNickname() != null))
                .and(SYS_USER.MOBILE.like(query.getMobile() != null ? "%" + query.getMobile() + "%" : null, query.getMobile() != null))
                .and(SYS_USER.DEPT_ID.eq(query.getDeptId(), query.getDeptId() != null))
                .and(SYS_USER.STATUS.eq(query.getStatus(), query.getStatus() != null))
                .orderBy(SYS_USER.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<SysUser> page = userMapper.paginate(
                query.getPageNum(), 
                query.getPageSize(), 
                wrapper);
        
        List<UserVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<UserVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public UserVO getUserDetail(Long id) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        UserVO vo = convertToVO(user);
        // 加载角色信息
        vo.setRoles(roleService.getRolesByUserId(id));
        return vo;
    }
    
    @Override
    public UserVO getByUsername(String username) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(SYS_USER)
                .where(SYS_USER.USERNAME.eq(username))
                .and(SYS_USER.DELETED.eq(0));
        
        SysUser user = userMapper.selectOneByQuery(wrapper);
        if (user == null) {
            return null;
        }
        
        UserVO vo = convertToVO(user);
        vo.setRoles(roleService.getRolesByUserId(user.getId()));
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER)
                .where(SYS_USER.USERNAME.eq(dto.getUsername()))
                .and(SYS_USER.DELETED.eq(0));
        
        if (count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        validateDeptPermission(dto.getDeptId());
        
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setCreateBy(StpUtil.getLoginIdAsString());
        user.setDeleted(0);
        
        save(user);
        
        if (!CollectionUtils.isEmpty(dto.getRoleIds())) {
            assignRoles(user.getId(), dto.getRoleIds());
        }
        
        log.info("创建用户成功：{}", user.getUsername());
        return user.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        validateUserPermission(id);
        validateDeptPermission(dto.getDeptId());
        
        BeanUtils.copyProperties(dto, user);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(user);
        
        // 重新分配角色
        if (dto.getRoleIds() != null) {
            assignRoles(id, dto.getRoleIds());
        }
        
        log.info("更新用户成功：{}", user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(id)) {
            throw new BusinessException("不能删除自己");
        }
        
        validateUserPermission(id);
        
        if (isLastEnabledSuperAdmin(id)) {
            throw new BusinessException("不能删除最后一个超级管理员用户");
        }
        
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(user);
        
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(id));
        userRoleMapper.deleteByQuery(deleteWrapper);
        
        log.info("删除用户成功：{}", user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        // 排除当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ids = ids.stream()
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toList());
        
        for (Long id : ids) {
            deleteUser(id);
        }
    }
    
    @Override
    public void enableUser(Long id) {
        updateStatus(id, 1);
    }
    
    @Override
    public void disableUser(Long id) {
        updateStatus(id, 0);
    }
    
    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        validateUserPermission(id);
        
        user.setPassword(BCrypt.hashpw(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(user);
        
        log.info("重置用户密码成功：{}", user.getUsername());
    }
    
    @Override
    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        
        // 验证新密码和确认密码
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }
        
        // 更新密码
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(user);
        
        log.info("修改用户密码成功：{}", user.getUsername());
    }
    
    @Override
    public void updateProfile(Long id, ProfileUpdateDTO dto) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 只更新非空字段
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getMobile() != null) {
            user.setMobile(dto.getMobile());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(user);
        
        log.info("更新个人信息成功：{}", user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        Long superAdminRoleId = getSuperAdminRoleId();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean isCurrentUserSuperAdmin = isUserSuperAdmin(currentUserId, superAdminRoleId);
        
        if (!isCurrentUserSuperAdmin && superAdminRoleId != null && roleIds != null && roleIds.contains(superAdminRoleId)) {
            throw new BusinessException("非超级管理员不能分配超级管理员角色");
        }
        
        boolean isCurrentlySuperAdmin = isUserSuperAdmin(userId, superAdminRoleId);
        boolean willBeSuperAdmin = superAdminRoleId != null && roleIds != null && roleIds.contains(superAdminRoleId);
        
        if (isCurrentlySuperAdmin && !willBeSuperAdmin) {
            if (isLastEnabledSuperAdmin(userId)) {
                throw new BusinessException("不能移除最后一个超级管理员用户的超级管理员角色");
            }
        }
        
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId));
        userRoleMapper.deleteByQuery(deleteWrapper);
        
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoles.add(userRole);
            }
            userRoleMapper.insertBatch(userRoles);
        }
    }
    
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        // 使用MyBatis-Flex查询用户的角色ID列表
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_USER_ROLE.ROLE_ID)
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId));
        
        List<SysUserRole> userRoles = userRoleMapper.selectListByQuery(wrapper);
        return userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新状态
     */
    private void updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        validateUserPermission(id);
        
        if (status == 0) {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (currentUserId.equals(id)) {
                throw new BusinessException("不能禁用自己");
            }
            
            if (isLastEnabledSuperAdmin(id)) {
                throw new BusinessException("不能禁用最后一个超级管理员用户");
            }
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(user);
        
        log.info("更新用户状态成功：{}，状态：{}", user.getUsername(), status);
    }
    
    /**
     * 转换为VO
     */
    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 设置状态文本
        vo.setStatusText(user.getStatus() == 1 ? "启用" : "禁用");
        
        // 查询部门名称
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectOneById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        
        return vo;
    }
    
    private Long getSuperAdminRoleId() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE.ID)
                .from(SYS_ROLE)
                .where(SYS_ROLE.ROLE_CODE.eq(SUPER_ADMIN_ROLE_CODE))
                .and(SYS_ROLE.DELETED.eq(0));
        
        SysRole role = roleMapper.selectOneByQuery(wrapper);
        return role != null ? role.getId() : null;
    }
    
    private boolean isUserSuperAdmin(Long userId, Long superAdminRoleId) {
        if (superAdminRoleId == null) {
            return false;
        }
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .and(SYS_USER_ROLE.ROLE_ID.eq(superAdminRoleId));
        
        return userRoleMapper.selectCountByQuery(wrapper) > 0;
    }
    
    private boolean isLastEnabledSuperAdmin(Long userId) {
        Long superAdminRoleId = getSuperAdminRoleId();
        if (superAdminRoleId == null) {
            return false;
        }
        
        // 检查该用户是否是超级管理员
        if (!isUserSuperAdmin(userId, superAdminRoleId)) {
            return false;
        }
        
        // 查询所有启用的超级管理员用户数量
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_USER.ID)
                .from(SYS_USER)
                .innerJoin(SYS_USER_ROLE).on(SYS_USER.ID.eq(SYS_USER_ROLE.USER_ID))
                .where(SYS_USER_ROLE.ROLE_ID.eq(superAdminRoleId))
                .and(SYS_USER.STATUS.eq(1))
                .and(SYS_USER.DELETED.eq(0));
        
        List<SysUser> superAdminUsers = userMapper.selectListByQuery(wrapper);
        
        return superAdminUsers.size() == 1;
    }
    
    @Override
    public PageVO<UserVO> pageQueryWithDataScope(UserQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(SYS_USER)
                .where(SYS_USER.DELETED.eq(0))
                .and(SYS_USER.USERNAME.like(query.getUsername() != null ? "%" + query.getUsername() + "%" : null, query.getUsername() != null))
                .and(SYS_USER.NICKNAME.like(query.getNickname() != null ? "%" + query.getNickname() + "%" : null, query.getNickname() != null))
                .and(SYS_USER.MOBILE.like(query.getMobile() != null ? "%" + query.getMobile() + "%" : null, query.getMobile() != null))
                .and(SYS_USER.DEPT_ID.eq(query.getDeptId(), query.getDeptId() != null))
                .and(SYS_USER.STATUS.eq(query.getStatus(), query.getStatus() != null));
        
        applyDataScope(wrapper);
        
        wrapper.orderBy(SYS_USER.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<SysUser> page = userMapper.paginate(
                query.getPageNum(), 
                query.getPageSize(), 
                wrapper);
        
        List<UserVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<UserVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    private void applyDataScope(QueryWrapper wrapper) {
        if (!StpUtil.isLogin()) {
            return;
        }
        
        DataScopeContext context = dataScopeHelper.getCurrentUserContext();
        Integer dataScopeValue = context.getDataScope();
        if (dataScopeValue == null) {
            dataScopeValue = 4;
        }
        
        switch (dataScopeValue) {
            case 1:
                break;
            case 2:
                List<Long> deptIds = context.getDeptIds();
                if (deptIds != null && !deptIds.isEmpty()) {
                    wrapper.and(SYS_USER.DEPT_ID.in(deptIds));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 3:
                if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 4:
                List<Long> deptAndChildrenIds = context.getDeptAndChildrenIds();
                if (deptAndChildrenIds != null && !deptAndChildrenIds.isEmpty()) {
                    wrapper.and(SYS_USER.DEPT_ID.in(deptAndChildrenIds));
                } else if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 5:
                wrapper.and(SYS_USER.ID.eq(context.getUserId()));
                break;
            default:
                if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                }
        }
    }
    
    @Override
    public List<Long> getAssignableRoleIds() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        if (isUserSuperAdmin(userId, getSuperAdminRoleId())) {
            QueryWrapper wrapper = QueryWrapper.create()
                    .select(SYS_ROLE.ID)
                    .from(SYS_ROLE)
                    .where(SYS_ROLE.DELETED.eq(0))
                    .and(SYS_ROLE.STATUS.eq(1));
            
            return roleMapper.selectListByQuery(wrapper).stream()
                    .map(SysRole::getId)
                    .collect(Collectors.toList());
        }
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE.ID)
                .from(SYS_ROLE)
                .where(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1))
                .and(SYS_ROLE.ROLE_CODE.ne(SUPER_ADMIN_ROLE_CODE));
        
        return roleMapper.selectListByQuery(wrapper).stream()
                .map(SysRole::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> getManagedDeptIds() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        if (isUserSuperAdmin(userId, getSuperAdminRoleId())) {
            QueryWrapper wrapper = QueryWrapper.create()
                    .select(SYS_DEPT.ID)
                    .from(SYS_DEPT)
                    .where(SYS_DEPT.DELETED.eq(0))
                    .and(SYS_DEPT.STATUS.eq(1));
            
            return deptMapper.selectListByQuery(wrapper).stream()
                    .map(SysDept::getId)
                    .collect(Collectors.toList());
        }
        
        DataScopeContext context = dataScopeHelper.buildContext(userId);
        Integer dataScopeValue = context.getDataScope();
        if (dataScopeValue == null) {
            dataScopeValue = 4;
        }
        
        switch (dataScopeValue) {
            case 1:
                QueryWrapper allWrapper = QueryWrapper.create()
                        .select(SYS_DEPT.ID)
                        .from(SYS_DEPT)
                        .where(SYS_DEPT.DELETED.eq(0))
                        .and(SYS_DEPT.STATUS.eq(1));
                return deptMapper.selectListByQuery(allWrapper).stream()
                        .map(SysDept::getId)
                        .collect(Collectors.toList());
            case 2:
                return context.getDeptIds() != null ? context.getDeptIds() : List.of();
            case 3:
                return context.getDeptId() != null ? List.of(context.getDeptId()) : List.of();
            case 4:
                return context.getDeptAndChildrenIds() != null ? context.getDeptAndChildrenIds() : List.of();
            case 5:
                return List.of();
            default:
                return context.getDeptId() != null ? List.of(context.getDeptId()) : List.of();
        }
    }
    
    private void validateDeptPermission(Long deptId) {
        if (deptId == null) {
            return;
        }
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (isUserSuperAdmin(currentUserId, getSuperAdminRoleId())) {
            return;
        }
        
        List<Long> managedDeptIds = getManagedDeptIds();
        if (!managedDeptIds.contains(deptId)) {
            throw new BusinessException("无权在该部门创建/编辑用户");
        }
    }
    
    private void validateUserPermission(Long targetUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long superAdminRoleId = getSuperAdminRoleId();
        
        if (isUserSuperAdmin(currentUserId, superAdminRoleId)) {
            return;
        }
        
        // 检查目标用户是否是超级管理员
        if (isUserSuperAdmin(targetUserId, superAdminRoleId)) {
            throw new BusinessException("无权操作超级管理员用户");
        }
        
        SysUser targetUser = getById(targetUserId);
        if (targetUser == null) {
            return;
        }
        
        List<Long> managedDeptIds = getManagedDeptIds();
        if (targetUser.getDeptId() != null && !managedDeptIds.contains(targetUser.getDeptId())) {
            throw new BusinessException("无权编辑该用户");
        }
    }
} 