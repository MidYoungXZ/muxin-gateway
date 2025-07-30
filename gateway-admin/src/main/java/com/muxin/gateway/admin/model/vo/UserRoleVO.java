package com.muxin.gateway.admin.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色信息VO
 */
public class UserRoleVO implements Serializable {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String mobile;
    private String avatar;
    private Integer gender;
    private Long deptId;
    private String deptName;
    private Integer status;
    private LocalDateTime createTime;
    private List<RoleInfo> roles;
    
    /**
     * 角色信息
     */
    public static class RoleInfo implements Serializable {
        private Long roleId;
        private String roleName;
        private String roleCode;
        
        // Getters and Setters
        public Long getRoleId() {
            return roleId;
        }
        
        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
        
        public String getRoleCode() {
            return roleCode;
        }
        
        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public Long getDeptId() {
        return deptId;
    }
    
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public List<RoleInfo> getRoles() {
        return roles;
    }
    
    public void setRoles(List<RoleInfo> roles) {
        this.roles = roles;
    }
} 