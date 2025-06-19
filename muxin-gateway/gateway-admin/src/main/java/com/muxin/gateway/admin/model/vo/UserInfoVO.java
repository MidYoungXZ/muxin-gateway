package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息VO
 *
 * @author muxin
 */
@Data
public class UserInfoVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
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
     * 部门名称
     */
    private String deptName;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 权限列表
     */
    private List<String> permissions;
    
    /**
     * 菜单树
     */
    private List<MenuVO> menus;
} 