package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体
 *
 * @author muxin
 */
@Data
@Table("sys_user")
public class SysUser {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
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
    
    /**
     * 用户角色列表（非数据库字段）
     */
    @RelationManyToMany(
            joinTable = "sys_user_role",
            selfField = "id",
            joinSelfColumn = "user_id",
            targetField = "id",
            joinTargetColumn = "role_id"
    )
    private List<SysRole> roles;
} 