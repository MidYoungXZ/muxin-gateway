package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建DTO
 *
 * @author muxin
 */
@Data
public class MenuCreateDTO {
    
    /**
     * 父菜单ID
     */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;
    
    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    
    /**
     * 国际化编码
     */
    private String i18nCode;
    
    /**
     * 菜单类型：M-目录，C-菜单，F-按钮
     */
    @NotBlank(message = "菜单类型不能为空")
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
    private Integer sortOrder = 0;
    
    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible = 1;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status = 1;
} 