package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色部门关联实体
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Table("sys_role_dept")
public class SysRoleDept {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}