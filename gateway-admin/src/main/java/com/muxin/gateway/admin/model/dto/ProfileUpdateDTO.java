package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人信息更新DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class ProfileUpdateDTO {
    
    /**
     * 昵称
     */
    @Size(min = 2, max = 20, message = "昵称长度在2-20个字符")
    private String nickname;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 头像URL
     */
    private String avatar;
}