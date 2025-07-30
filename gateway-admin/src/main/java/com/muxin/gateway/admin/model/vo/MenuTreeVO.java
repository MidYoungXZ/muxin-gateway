package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单树VO
 *
 * @author muxin
 */
@Data
public class MenuTreeVO {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 国际化编码
     */
    private String i18nCode;
    
    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 是否显示
     */
    private Integer visible;
    
    /**
     * 子菜单
     */
    private List<MenuTreeVO> children;
} 