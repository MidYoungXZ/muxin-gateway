package com.muxin.gateway.admin.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 过期时间（毫秒）
     */
    private long expiresIn;

    /**
     * 登录时间（毫秒）
     */
    private long loginTime;
} 