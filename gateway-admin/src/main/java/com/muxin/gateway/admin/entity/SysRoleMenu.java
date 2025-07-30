package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 *
 * @author muxin
 */
@Data
@Table("sys_role_menu")
public class SysRoleMenu {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 菜单ID
     */
    private Long menuId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 