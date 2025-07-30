package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 更新用户DTO
 *
 * @author muxin
 */
@Data
public class UserUpdateDTO {
    
    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
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
     * 角色ID列表
     */
    private List<Long> roleIds;
} 