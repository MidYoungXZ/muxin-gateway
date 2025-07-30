package com.muxin.gateway.admin.model.dto;

import lombok.Data;

/**
 * 菜单更新DTO
 *
 * @author muxin
 */
@Data
public class MenuUpdateDTO {
    
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
     * 菜单类型：M-目录，C-菜单，F-按钮
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
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
} 