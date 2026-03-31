package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysRole;
import com.muxin.gateway.admin.entity.SysRoleDept;
import com.muxin.gateway.admin.entity.SysRoleMenu;
import com.muxin.gateway.admin.entity.SysUserRole;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.RoleDeptMapper;
import com.muxin.gateway.admin.mapper.RoleMapper;
import com.muxin.gateway.admin.mapper.RoleMenuMapper;
import com.muxin.gateway.admin.mapper.UserRoleMapper;
import com.muxin.gateway.admin.model.dto.RoleCreateDTO;
import com.muxin.gateway.admin.model.dto.RoleQueryDTO;
import com.muxin.gateway.admin.model.dto.RoleUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RoleVO;
import com.muxin.gateway.admin.service.MenuService;
import com.muxin.gateway.admin.service.RoleService;
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
 * 角色服务实现
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, SysRole> implements RoleService {
    
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";
    
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final MenuService menuService;
    
    @Override
    public PageVO<RoleVO> pageQuery(RoleQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.DELETED.eq(0));
        
        // 动态条件
        if (StringUtils.hasText(query.getRoleName())) {
            wrapper.and(SYS_ROLE.ROLE_NAME.like("%" + query.getRoleName() + "%"));
        }
        
        if (StringUtils.hasText(query.getRoleCode())) {
            wrapper.and(SYS_ROLE.ROLE_CODE.like("%" + query.getRoleCode() + "%"));
        }
        
        if (query.getStatus() != null) {
            wrapper.and(SYS_ROLE.STATUS.eq(query.getStatus()));
        }
        
        // 排序
        wrapper.orderBy(SYS_ROLE.CREATE_TIME.desc());
        
        // 分页查询
        com.mybatisflex.core.paginate.Page<SysRole> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        // 转换为VO
        List<RoleVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<RoleVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public List<RoleVO> listAll() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1))
                .orderBy(SYS_ROLE.CREATE_TIME.desc());
        
        List<SysRole> roles = list(wrapper);
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleVO getRoleDetail(Long id) {
        SysRole role = getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        
        RoleVO vo = convertToVO(role);
        vo.setMenus(menuService.getMenusByRoleId(id));
        
        if (role.getDataScope() != null && role.getDataScope() == 2) {
            vo.setDeptIds(getRoleDeptIds(id));
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateDTO dto) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.ROLE_CODE.eq(dto.getRoleCode()))
                .and(SYS_ROLE.DELETED.eq(0));
        
        if (count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(1);
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 4);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setCreateBy(StpUtil.getLoginIdAsString());
        role.setDeleted(0);
        
        save(role);
        
        if (!CollectionUtils.isEmpty(dto.getMenuIds())) {
            assignMenus(role.getId(), dto.getMenuIds());
        }
        
        if (dto.getDataScope() != null && dto.getDataScope() == 2 && !CollectionUtils.isEmpty(dto.getDeptIds())) {
            assignDepts(role.getId(), dto.getDeptIds());
        }
        
        log.info("创建角色成功：{}", role.getRoleName());
        return role.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleUpdateDTO dto) {
        SysRole role = getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        
        BeanUtils.copyProperties(dto, role);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(role);
        
        if (dto.getMenuIds() != null) {
            assignMenus(id, dto.getMenuIds());
        }
        
        if (dto.getDataScope() != null) {
            if (dto.getDataScope() == 2 && !CollectionUtils.isEmpty(dto.getDeptIds())) {
                assignDepts(id, dto.getDeptIds());
            } else {
                assignDepts(id, new ArrayList<>());
            }
        }
        
        log.info("更新角色成功：{}", role.getRoleName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        
        if (isSuperAdminRole(role)) {
            throw new BusinessException("超级管理员角色不允许删除");
        }
        
        // 检查是否有用户使用该角色（使用MyBatis-Flex查询）
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.ROLE_ID.eq(id));
        
        long userCount = userRoleMapper.selectCountByQuery(wrapper);
        if (userCount > 0) {
            throw new BusinessException("该角色下还有" + userCount + "个用户，无法删除");
        }
        
        // 逻辑删除
        role.setDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(role);
        
        // 删除角色菜单关联（使用MyBatis-Flex删除）
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_ROLE_MENU)
                .where(SYS_ROLE_MENU.ROLE_ID.eq(id));
        roleMenuMapper.deleteByQuery(deleteWrapper);
        
        log.info("删除角色成功：{}", role.getRoleName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        for (Long id : ids) {
            deleteRole(id);
        }
    }
    
    @Override
    public void enableRole(Long id) {
        updateStatus(id, 1);
    }
    
    @Override
    public void disableRole(Long id) {
        updateStatus(id, 0);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有的角色菜单关联（使用MyBatis-Flex删除）
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_ROLE_MENU)
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId));
        roleMenuMapper.deleteByQuery(deleteWrapper);
        
        // 创建新的关联
        if (!CollectionUtils.isEmpty(menuIds)) {
            List<SysRoleMenu> roleMenus = new ArrayList<>();
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateTime(LocalDateTime.now());
                roleMenus.add(roleMenu);
            }
            roleMenuMapper.insertBatch(roleMenus);
        }
    }
    
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE_MENU.MENU_ID)
                .from(SYS_ROLE_MENU)
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId));
        
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectListByQuery(wrapper);
        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDepts(Long roleId, List<Long> deptIds) {
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .from(SYS_ROLE_DEPT)
                .where(SYS_ROLE_DEPT.ROLE_ID.eq(roleId));
        roleDeptMapper.deleteByQuery(deleteWrapper);
        
        if (!CollectionUtils.isEmpty(deptIds)) {
            List<SysRoleDept> roleDepts = new ArrayList<>();
            for (Long deptId : deptIds) {
                SysRoleDept roleDept = new SysRoleDept();
                roleDept.setRoleId(roleId);
                roleDept.setDeptId(deptId);
                roleDept.setCreateTime(LocalDateTime.now());
                roleDepts.add(roleDept);
            }
            roleDeptMapper.insertBatch(roleDepts);
        }
    }
    
    @Override
    public List<Long> getRoleDeptIds(Long roleId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE_DEPT.DEPT_ID)
                .from(SYS_ROLE_DEPT)
                .where(SYS_ROLE_DEPT.ROLE_ID.eq(roleId));
        
        List<SysRoleDept> roleDepts = roleDeptMapper.selectListByQuery(wrapper);
        return roleDepts.stream()
                .map(SysRoleDept::getDeptId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RoleVO> getRolesByUserId(Long userId) {
        // 使用MyBatis-Flex多表查询
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_ROLE.ALL_COLUMNS)
                .from(SYS_ROLE)
                .innerJoin(SYS_USER_ROLE).on(SYS_ROLE.ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .and(SYS_ROLE.DELETED.eq(0));
        
        List<SysRole> roles = roleMapper.selectListByQuery(wrapper);
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_USER_ROLE.ROLE_ID)
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId));
        List<SysUserRole> userRoles = userRoleMapper.selectListByQuery(wrapper);
        return userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }
    
    /**
     * 更新状态
     */
    private void updateStatus(Long id, Integer status) {
        SysRole role = getById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        
        if (status == 0 && isSuperAdminRole(role)) {
            throw new BusinessException("超级管理员角色不允许禁用");
        }
        
        role.setStatus(status);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(role);
        
        log.info("更新角色状态成功：{}，状态：{}", role.getRoleName(), status);
    }
    
    private boolean isSuperAdminRole(SysRole role) {
        return SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode());
    }
    
    /**
     * 转换为VO
     */
    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        
        vo.setStatusText(role.getStatus() == 1 ? "启用" : "禁用");
        vo.setDataScopeText(getDataScopeText(role.getDataScope()));
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.ROLE_ID.eq(role.getId()));
        
        vo.setUserCount(userRoleMapper.selectCountByQuery(wrapper));
        
        return vo;
    }
    
    private String getDataScopeText(Integer dataScope) {
        if (dataScope == null) return "本部门及以下";
        switch (dataScope) {
            case 1: return "全部数据";
            case 2: return "自定义数据";
            case 3: return "本部门数据";
            case 4: return "本部门及以下";
            case 5: return "仅本人数据";
            default: return "本部门及以下";
        }
    }
    
    @Override
    public Boolean checkRoleCodeAvailable(String roleCode, Long excludeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.ROLE_CODE.eq(roleCode))
                .and(SYS_ROLE.DELETED.eq(0));
        
        if (excludeId != null) {
            wrapper.and(SYS_ROLE.ID.ne(excludeId));
        }
        
        return count(wrapper) == 0;
    }
    
    @Override
    public Object getRoleStats() {
        // 总角色数
        QueryWrapper totalWrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.DELETED.eq(0));
        long totalRoles = count(totalWrapper);
        
        // 启用角色数
        QueryWrapper enabledWrapper = QueryWrapper.create()
                .select()
                .from(SYS_ROLE)
                .where(SYS_ROLE.DELETED.eq(0))
                .and(SYS_ROLE.STATUS.eq(1));
        long enabledRoles = count(enabledWrapper);
        
        // 禁用角色数
        long disabledRoles = totalRoles - enabledRoles;
        
        // 返回统计信息
        return new Object() {
            public final long total = totalRoles;
            public final long enabled = enabledRoles;
            public final long disabled = disabledRoles;
        };
    }
} 