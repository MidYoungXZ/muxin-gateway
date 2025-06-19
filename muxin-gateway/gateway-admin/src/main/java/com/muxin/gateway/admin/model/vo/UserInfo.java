package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息VO
 *
 * @author muxin
 */
@Data
public class UserInfo {
    
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Long deptId;
    
    private String deptName;
    
    private List<String> roles;
    
    private List<String> permissions;
} 