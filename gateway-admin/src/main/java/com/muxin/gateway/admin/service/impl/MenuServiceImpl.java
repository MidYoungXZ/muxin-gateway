package com.muxin.gateway.admin.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysMenu;
import com.muxin.gateway.admin.entity.SysRoleMenu;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.mapper.MenuMapper;
import com.muxin.gateway.admin.mapper.RoleMenuMapper;
import com.muxin.gateway.admin.model.dto.MenuCreateDTO;
import com.muxin.gateway.admin.model.dto.MenuQueryDTO;
import com.muxin.gateway.admin.model.dto.MenuUpdateDTO;
import com.muxin.gateway.admin.model.vo.MenuVO;
import com.muxin.gateway.admin.model.vo.MenuTreeVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.MenuService;
import com.muxin.gateway.admin.entity.SysUserRole;
import com.muxin.gateway.admin.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, SysMenu> implements MenuService {
    
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public List<MenuVO> getMenusByRoleId(Long roleId) {
        // 使用MyBatis-Flex多表查询
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_MENU.ALL_COLUMNS)
                .from(SYS_MENU)
                .innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId))
                .and(SYS_MENU.DELETED.eq(0))
                .and(SYS_MENU.STATUS.eq(1))
                .orderBy(SYS_MENU.SORT_ORDER.asc());
        
        List<SysMenu> menus = menuMapper.selectListByQuery(wrapper);
        
        // 转换为VO并构建树形结构
        List<MenuVO> menuVOs = menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return buildMenuTree(menuVOs);
    }
    

    
    @Override
    public List<MenuVO> getAllMenuTree() {
        // 查询所有菜单
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_MENU)
                .where(SYS_MENU.DELETED.eq(0))
                .orderBy(SYS_MENU.SORT_ORDER.asc());
        
        List<SysMenu> menus = list(wrapper);
        
        // 转换为VO并构建树形结构
        List<MenuVO> menuVOs = menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return buildMenuTree(menuVOs);
    }
    
    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> menus) {
        // 创建ID到菜单的映射
        Map<Long, MenuVO> menuMap = menus.stream()
                .collect(Collectors.toMap(MenuVO::getId, menu -> menu));
        
        // 构建树形结构
        List<MenuVO> tree = new ArrayList<>();
        for (MenuVO menu : menus) {
            if (menu.getParentId() == 0) {
                // 根节点
                tree.add(menu);
            } else {
                // 找到父节点并添加为子节点
                MenuVO parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        
        // 递归排序子菜单
        sortMenuTree(tree);
        
        return tree;
    }
    
    /**
     * 递归排序菜单树
     */
    private void sortMenuTree(List<MenuVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        
        // 按sortOrder排序
        menus.sort(Comparator.comparing(MenuVO::getSortOrder));
        
        // 递归排序子菜单
        for (MenuVO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenuTree(menu.getChildren());
            }
        }
    }
    
    /**
     * 转换为VO
     */
    private MenuVO convertToVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
    
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        // 查询角色关联的菜单ID列表
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
    public List<String> getUserPermissions(Long userId) {
        // 直接查询用户的角色ID列表，避免循环依赖
        QueryWrapper roleWrapper = QueryWrapper.create()
                .select(SYS_USER_ROLE.ROLE_ID)
                .from(SYS_USER_ROLE)
                .where(SYS_USER_ROLE.USER_ID.eq(userId));
        List<SysUserRole> userRoles = userRoleMapper.selectListByQuery(roleWrapper);
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        
        // 查询角色关联的菜单权限
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_MENU.PERMS)
                .from(SYS_MENU)
                .innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))
                .where(SYS_ROLE_MENU.ROLE_ID.in(roleIds))
                .and(SYS_MENU.STATUS.eq(1))
                .and(SYS_MENU.DELETED.eq(0))
                .and(SYS_MENU.PERMS.isNotNull())
                .and(SYS_MENU.PERMS.ne(""));
        
        List<SysMenu> menus = list(wrapper);
        
        // 提取权限标识
        return menus.stream()
                .map(SysMenu::getPerms)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MenuVO> getUserMenuTree(Long userId) {
        // 使用MyBatis-Flex多表查询获取用户的所有菜单
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_MENU.ALL_COLUMNS)
                .from(SYS_MENU)
                .innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))
                .innerJoin(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .and(SYS_MENU.DELETED.eq(0))
                .and(SYS_MENU.STATUS.eq(1))
                .and(SYS_MENU.VISIBLE.eq(1))
                .orderBy(SYS_MENU.SORT_ORDER.asc());
        
        List<SysMenu> menus = menuMapper.selectListByQuery(wrapper);
        
        // 转换为VO并构建树形结构
        List<MenuVO> menuVOs = menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return buildMenuTree(menuVOs);
    }
    
    @Override
    public List<MenuTreeVO> getUserMenuTreeVO(Long userId) {
        // 使用MyBatis-Flex多表查询获取用户的所有菜单
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SYS_MENU.ALL_COLUMNS)
                .from(SYS_MENU)
                .innerJoin(SYS_ROLE_MENU).on(SYS_MENU.ID.eq(SYS_ROLE_MENU.MENU_ID))
                .innerJoin(SYS_USER_ROLE).on(SYS_ROLE_MENU.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
                .where(SYS_USER_ROLE.USER_ID.eq(userId))
                .and(SYS_MENU.DELETED.eq(0))
                .and(SYS_MENU.STATUS.eq(1))
                .and(SYS_MENU.VISIBLE.eq(1))
                .orderBy(SYS_MENU.SORT_ORDER.asc());
        
        List<SysMenu> menus = menuMapper.selectListByQuery(wrapper);
        
        // 转换为VO并构建树形结构
        List<MenuTreeVO> menuTreeVOs = menus.stream()
                .map(this::convertToMenuTreeVO)
                .collect(Collectors.toList());
        
        return buildMenuTreeList(menuTreeVOs);
    }
    
    /**
     * 转换为MenuTreeVO
     */
    private MenuTreeVO convertToMenuTreeVO(SysMenu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
    
    /**
     * 构建菜单树列表
     */
    private List<MenuTreeVO> buildMenuTreeList(List<MenuTreeVO> menus) {
        // 创建ID到菜单的映射
        Map<Long, MenuTreeVO> menuMap = menus.stream()
                .collect(Collectors.toMap(MenuTreeVO::getId, menu -> menu));
        
        // 构建树形结构
        List<MenuTreeVO> tree = new ArrayList<>();
        for (MenuTreeVO menu : menus) {
            if (menu.getParentId() == 0) {
                // 根节点
                tree.add(menu);
            } else {
                // 找到父节点并添加为子节点
                MenuTreeVO parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        
        // 递归排序子菜单
        sortMenuTreeList(tree);
        
        return tree;
    }
    
    /**
     * 递归排序菜单树列表
     */
    private void sortMenuTreeList(List<MenuTreeVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        
        // 按sortOrder排序
        menus.sort(Comparator.comparing(MenuTreeVO::getSortOrder));
        
        // 递归排序子菜单
        for (MenuTreeVO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenuTreeList(menu.getChildren());
            }
        }
    }
    
    @Override
    public PageVO<MenuVO> pageQuery(MenuQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(SYS_MENU)
                .where(SYS_MENU.DELETED.eq(0))
                .orderBy(SYS_MENU.SORT_ORDER.asc());
        
        // 添加查询条件
        if (StringUtils.hasText(query.getMenuName())) {
            wrapper.and(SYS_MENU.MENU_NAME.like(query.getMenuName()));
        }
        if (StringUtils.hasText(query.getMenuType())) {
            wrapper.and(SYS_MENU.MENU_TYPE.eq(query.getMenuType()));
        }
        if (query.getStatus() != null) {
            wrapper.and(SYS_MENU.STATUS.eq(query.getStatus()));
        }
        if (query.getVisible() != null) {
            wrapper.and(SYS_MENU.VISIBLE.eq(query.getVisible()));
        }
        if (query.getParentId() != null) {
            wrapper.and(SYS_MENU.PARENT_ID.eq(query.getParentId()));
        }
        if (StringUtils.hasText(query.getPerms())) {
            wrapper.and(SYS_MENU.PERMS.like(query.getPerms()));
        }
        
        Page<SysMenu> page = Page.of(query.getPageNum(), query.getPageSize());
        Page<SysMenu> menuPage = page(page, wrapper);
        
        List<MenuVO> menuVOs = menuPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<MenuVO>builder()
                .data(menuVOs)
                .total(menuPage.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) Math.ceil((double) menuPage.getTotalRow() / query.getPageSize()))
                .hasNext(menuPage.getTotalRow() > (long) query.getPageNum() * query.getPageSize())
                .build();
    }
    
    @Override
    public MenuVO getMenuDetail(Long id) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        return convertToVO(menu);
    }
    
    @Override
    @Transactional
    public Long createMenu(MenuCreateDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        
        // 设置创建时间
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menu.setDeleted(0);
        
        // 更新祖级列表（ancestors）
        updateAncestors(menu);
        
        save(menu);
        return menu.getId();
    }
    
    @Override
    @Transactional
    public void updateMenu(Long id, MenuUpdateDTO dto) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        
        // 检查父菜单是否变更
        Long oldParentId = menu.getParentId();
        BeanUtils.copyProperties(dto, menu, "id", "createTime");
        menu.setUpdateTime(LocalDateTime.now());
        
        // 如果父菜单变更，需要更新祖级列表
        if (!java.util.Objects.equals(oldParentId, menu.getParentId())) {
            updateAncestors(menu);
            // 同时更新所有子菜单的祖级列表
            updateChildrenAncestors(id);
        }
        
        updateById(menu);
    }
    
    @Override
    @Transactional
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        long childCount = count(QueryWrapper.create()
                .from(SYS_MENU)
                .where(SYS_MENU.PARENT_ID.eq(id))
                .and(SYS_MENU.DELETED.eq(0)));
        
        if (childCount > 0) {
            throw new RuntimeException("存在子菜单，不能删除");
        }
        
        // 检查是否被角色引用
        long roleMenuCount = roleMenuMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(SYS_ROLE_MENU)
                        .where(SYS_ROLE_MENU.MENU_ID.eq(id))
        );
        
        if (roleMenuCount > 0) {
            throw new RuntimeException("菜单已被角色引用，不能删除");
        }
        
        // 逻辑删除
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setDeleted(1);
        menu.setUpdateTime(LocalDateTime.now());
        updateById(menu);
    }
    
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteMenu(id);
        }
    }
    
    @Override
    public void enableMenu(Long id) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setStatus(1);
        menu.setUpdateTime(LocalDateTime.now());
        updateById(menu);
    }
    
    @Override
    public void disableMenu(Long id) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setStatus(0);
        menu.setUpdateTime(LocalDateTime.now());
        updateById(menu);
    }
    
    @Override
    @Transactional
    public void moveMenu(Long id, Long targetParentId) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        
        // 不能移动到自己的子菜单下
        if (isDescendant(targetParentId, id)) {
            throw new RuntimeException("不能移动到自己的子菜单下");
        }
        
        menu.setParentId(targetParentId);
        menu.setUpdateTime(LocalDateTime.now());
        
        // 更新祖级列表
        updateAncestors(menu);
        updateById(menu);
        
        // 更新所有子菜单的祖级列表
        updateChildrenAncestors(id);
    }
    
    @Override
    public void updateMenuSort(Long id, Integer sortOrder) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setSortOrder(sortOrder);
        menu.setUpdateTime(LocalDateTime.now());
        updateById(menu);
    }
    
    /**
     * 更新菜单的祖级列表
     */
    private void updateAncestors(SysMenu menu) {
        if (menu.getParentId() == 0) {
            menu.setAncestors("0");
        } else {
            SysMenu parent = getById(menu.getParentId());
            if (parent != null) {
                menu.setAncestors(parent.getAncestors() + "," + menu.getParentId());
            } else {
                menu.setAncestors("0");
            }
        }
    }
    
    /**
     * 更新子菜单的祖级列表
     */
    private void updateChildrenAncestors(Long parentId) {
        List<SysMenu> children = list(QueryWrapper.create()
                .from(SYS_MENU)
                .where(SYS_MENU.PARENT_ID.eq(parentId))
                .and(SYS_MENU.DELETED.eq(0)));
        
        for (SysMenu child : children) {
            updateAncestors(child);
            updateById(child);
            // 递归更新
            updateChildrenAncestors(child.getId());
        }
    }
    
    /**
     * 检查target是否是source的后代
     */
    private boolean isDescendant(Long target, Long source) {
        if (target == null || source == null || target.equals(source)) {
            return false;
        }
        
        SysMenu targetMenu = getById(target);
        if (targetMenu == null) {
            return false;
        }
        
        // 检查祖级列表中是否包含source
        String ancestors = targetMenu.getAncestors();
        if (StringUtils.hasText(ancestors)) {
            String[] ancestorArray = ancestors.split(",");
            for (String ancestor : ancestorArray) {
                if (source.toString().equals(ancestor)) {
                    return true;
                }
            }
        }
        
        return false;
    }
} 