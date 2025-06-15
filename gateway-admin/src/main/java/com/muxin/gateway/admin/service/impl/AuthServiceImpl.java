package com.muxin.gateway.admin.service.impl;

import com.muxin.gateway.admin.config.AdminProperties;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.model.auth.LoginRequest;
import com.muxin.gateway.admin.model.auth.LoginResponse;
import com.muxin.gateway.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AdminProperties adminProperties;
    
    /**
     * 会话存储
     */
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 验证用户名密码
        if (adminProperties.getUsername().equals(loginRequest.getUsername()) &&
                adminProperties.getPassword().equals(loginRequest.getPassword())) {

            // 创建会话
            String sessionId = UUID.randomUUID().toString();
            long loginTime = System.currentTimeMillis();
            long expiresIn = adminProperties.getSessionTimeout() * 60 * 1000L; // 转换为毫秒

            // 保存会话信息
            SessionInfo sessionInfo = new SessionInfo(
                    loginRequest.getUsername(),
                    loginTime,
                    expiresIn
            );
            sessions.put(sessionId, sessionInfo);

            // 清理过期会话
            cleanExpiredSessions();

            log.info("用户 [{}] 登录成功", loginRequest.getUsername());
            
            return LoginResponse.builder()
                    .sessionId(sessionId)
                    .username(loginRequest.getUsername())
                    .loginTime(loginTime)
                    .expiresIn(expiresIn)
                    .build();
        } else {
            log.warn("用户 [{}] 登录失败，用户名或密码错误", loginRequest.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    @Override
    public void logout(String sessionId) {
        if (sessionId != null) {
            SessionInfo removedSession = sessions.remove(sessionId);
            if (removedSession != null) {
                log.info("用户 [{}] 退出登录", removedSession.getUsername());
            }
        }
    }

    @Override
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo == null) {
            return false;
        }
        
        // 检查会话是否过期
        long now = System.currentTimeMillis();
        if (now > sessionInfo.getLoginTime() + sessionInfo.getExpiresIn()) {
            sessions.remove(sessionId);
            log.debug("会话 [{}] 已过期", sessionId);
            return false;
        }
        
        return true;
    }

    @Override
    public LoginResponse getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        
        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo == null || !isSessionValid(sessionId)) {
            return null;
        }
        
        return LoginResponse.builder()
                .sessionId(sessionId)
                .username(sessionInfo.getUsername())
                .loginTime(sessionInfo.getLoginTime())
                .expiresIn(sessionInfo.getExpiresIn())
                .build();
    }
    
    /**
     * 清理过期的会话
     */
    private void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> 
            now > entry.getValue().getLoginTime() + entry.getValue().getExpiresIn());
    }
    
    /**
     * 会话信息
     */
    private static class SessionInfo {
        private final String username;
        private final long loginTime;
        private final long expiresIn;
        
        public SessionInfo(String username, long loginTime, long expiresIn) {
            this.username = username;
            this.loginTime = loginTime;
            this.expiresIn = expiresIn;
        }
        
        public String getUsername() {
            return username;
        }
        
        public long getLoginTime() {
            return loginTime;
        }
        
        public long getExpiresIn() {
            return expiresIn;
        }
    }
} 