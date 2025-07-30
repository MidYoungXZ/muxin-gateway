package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体
 *
 * @author muxin
 */
@Data
@Table("sys_menu")
public class SysMenu {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 祖级列表
     */
    private String ancestors;
    
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
    @Column("sort_order")
    private Integer sortOrder;
    
    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 是否删除：0-否，1-是
     */
    @Column(isLogicDelete = true)
    private Integer deleted;
} 