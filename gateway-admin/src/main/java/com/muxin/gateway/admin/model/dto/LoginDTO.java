package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class LoginDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 验证码（可选）
     */
    private String captcha;
    
    /**
     * 验证码key（可选）
     */
    private String captchaKey;
} 