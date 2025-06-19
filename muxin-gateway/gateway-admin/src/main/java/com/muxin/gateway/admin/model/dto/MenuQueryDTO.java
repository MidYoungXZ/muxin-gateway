package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单查询DTO
 *
 * @author muxin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuQueryDTO extends PageQueryDTO {
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 菜单类型：M-目录，C-菜单，F-按钮
     */
    private String menuType;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 权限标识
     */
    private String perms;
} 