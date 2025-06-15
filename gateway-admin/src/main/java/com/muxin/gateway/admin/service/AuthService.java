package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.auth.LoginRequest;
import com.muxin.gateway.admin.model.auth.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 退出登录
     *
     * @param sessionId 会话ID
     */
    void logout(String sessionId);

    /**
     * 检查会话是否有效
     *
     * @param sessionId 会话ID
     * @return 是否有效
     */
    boolean isSessionValid(String sessionId);

    /**
     * 获取会话信息
     * 
     * @param sessionId 会话ID
     * @return 会话信息
     */
    LoginResponse getSession(String sessionId);
} 