package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 *
 * @author muxin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQueryDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
} 