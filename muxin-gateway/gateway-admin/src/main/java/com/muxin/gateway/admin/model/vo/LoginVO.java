package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应VO
 *
 * @author muxin
 */
@Data
@Builder
public class LoginVO {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 访问令牌（兼容旧版本）
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 过期时间（秒）
     */
    private Long expireTime;
    
    /**
     * 过期时间（秒）（兼容旧版本）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfoVO userInfo;
} 