package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysDept;
import com.muxin.gateway.admin.entity.SysUser;
import com.muxin.gateway.admin.entity.SysUserRole;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.DeptMapper;
import com.muxin.gateway.admin.mapper.UserMapper;
import com.muxin.gateway.admin.mapper.UserRoleMapper;
import com.muxin.gateway.admin.model.dto.PasswordUpdateDTO;
import com.muxin.gateway.admin.model.dto.UserCreateDTO;
import com.muxin.gateway.admin.model.dto.UserQueryDTO;
import com.muxin.gateway.admin.model.dto.UserUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RoleVO;
import com.muxin.gateway.admin.model.vo.UserVO;
import com.muxin.gateway.admin.service.RoleService;
import com.muxin.gateway.admin.service.UserService;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final RoleService roleService;
    
    @Override
    public PageVO<UserVO> pageQuery(UserQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER)
                .where(SYS_USER.DELETED.eq(0));
        
        // 动态条件
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.and(SYS_USER.USERNAME.like("%" + query.getUsername() + "%"));
        }
        
        if (StringUtils.hasText(query.getNickname())) {
            wrapper.and(SYS_USER.NICKNAME.like("%" + query.getNickname() + "%"));
        }
        
        if (StringUtils.hasText(query.getMobile())) {
            wrapper.and(SYS_USER.MOBILE.like("%" + query.getMobile() + "%"));
        }
        
        if (query.getDeptId() != null) {
            wrapper.and(SYS_USER.DEPT_ID.eq(query.getDeptId()));
        }
        
        if (query.getStatus() != null) {
            wrapper.and(SYS_USER.STATUS.eq(query.getStatus()));
        }
        
        // 排序
        wrapper.orderBy(SYS_USER.CREATE_TIME.desc());
        
        // 分页查询
        com.mybatisflex.core.paginate.Page<SysUser> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        // 转换为VO
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
                .select()
                .from(SYS_USER)
                .where(SYS_USER.USERNAME.eq(username))
                .and(SYS_USER.DELETED.eq(0));
        
        SysUser user = getOne(wrapper);
        if (user == null) {
            return null;
        }
        
        UserVO vo = convertToVO(user);
        // 加载角色信息
        vo.setRoles(roleService.getRolesByUserId(user.getId()));
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        // 检查用户名是否已存在
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER)
                .where(SYS_USER.USERNAME.eq(dto.getUsername()))
                .and(SYS_USER.DELETED.eq(0));
        
        if (count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        
        // 密码加密
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setCreateBy(StpUtil.getLoginIdAsString());
        user.setDeleted(0);
        
        save(user);
        
        // 分配角色
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
        
        // 更新用户信息
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
        
        // 不能删除自己
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(id)) {
            throw new BusinessException("不能删除自己");
        }
        
        // 逻辑删除
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(user);
        
        // 删除用户角色关联
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
        
        // 密码加密
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
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有的用户角色关联
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId));
        userRoleMapper.deleteByQuery(deleteWrapper);
        
        // 创建新的关联
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
        
        // 不能禁用自己
        if (status == 0) {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (currentUserId.equals(id)) {
                throw new BusinessException("不能禁用自己");
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
} 