package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.RoleCreateDTO;
import com.muxin.gateway.admin.model.dto.RoleQueryDTO;
import com.muxin.gateway.admin.model.dto.RoleUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author muxin
 */
public interface RoleService {
    
    /**
     * 分页查询角色列表
     */
    PageVO<RoleVO> pageQuery(RoleQueryDTO query);
    
    /**
     * 获取所有角色列表
     */
    List<RoleVO> listAll();
    
    /**
     * 获取角色详情
     */
    RoleVO getRoleDetail(Long id);
    
    /**
     * 创建角色
     */
    Long createRole(RoleCreateDTO dto);
    
    /**
     * 更新角色
     */
    void updateRole(Long id, RoleUpdateDTO dto);
    
    /**
     * 删除角色
     */
    void deleteRole(Long id);
    
    /**
     * 批量删除角色
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 启用角色
     */
    void enableRole(Long id);
    
    /**
     * 禁用角色
     */
    void disableRole(Long id);
    
    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);
    
    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);
    
    /**
     * 根据用户ID获取角色列表
     */
    List<RoleVO> getRolesByUserId(Long userId);
    
    /**
     * 根据用户ID获取角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
    
    /**
     * 检查角色编码是否可用
     */
    Boolean checkRoleCodeAvailable(String roleCode, Long excludeId);
    
    /**
     * 获取角色统计信息
     */
    Object getRoleStats();
} 