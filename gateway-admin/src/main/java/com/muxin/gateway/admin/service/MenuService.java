package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.MenuCreateDTO;
import com.muxin.gateway.admin.model.dto.MenuQueryDTO;
import com.muxin.gateway.admin.model.dto.MenuUpdateDTO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;
import com.muxin.gateway.admin.model.vo.MenuVO;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author muxin
 */
public interface MenuService {
    
    /**
     * 根据角色ID获取菜单列表
     */
    List<MenuVO> getMenusByRoleId(Long roleId);
    
    /**
     * 获取用户菜单树
     */
    List<MenuVO> getUserMenuTree(Long userId);
    
    /**
     * 获取用户菜单树（返回MenuTreeVO）
     */
    List<MenuTreeVO> getUserMenuTreeVO(Long userId);
    
    /**
     * 获取所有菜单树
     */
    List<MenuVO> getAllMenuTree();
    
    /**
     * 根据角色ID获取菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
    
    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 分页查询菜单列表
     */
    PageVO<MenuVO> pageQuery(MenuQueryDTO query);
    
    /**
     * 获取菜单详情
     */
    MenuVO getMenuDetail(Long id);
    
    /**
     * 创建菜单
     */
    Long createMenu(MenuCreateDTO dto);
    
    /**
     * 更新菜单
     */
    void updateMenu(Long id, MenuUpdateDTO dto);
    
    /**
     * 删除菜单
     */
    void deleteMenu(Long id);
    
    /**
     * 批量删除菜单
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 启用菜单
     */
    void enableMenu(Long id);
    
    /**
     * 禁用菜单
     */
    void disableMenu(Long id);
    
    /**
     * 移动菜单
     */
    void moveMenu(Long id, Long targetParentId);
    
    /**
     * 调整菜单排序
     */
    void updateMenuSort(Long id, Integer sortOrder);
} 