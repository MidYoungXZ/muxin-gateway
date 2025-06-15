package com.muxin.gateway.admin.controller;

import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.auth.LoginRequest;
import com.muxin.gateway.admin.model.auth.LoginResponse;
import com.muxin.gateway.admin.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String SESSION_COOKIE_NAME = "GATEWAY_SESSION_ID";
    
    private final AuthService authService;

    /**
     * 登录 - RESTful API方式，接收JSON格式数据
     */
    @PostMapping(value = "/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                     HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            
            // 设置Cookie
            Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, loginResponse.getSessionId());
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            sessionCookie.setMaxAge((int)(loginResponse.getExpiresIn() / 1000)); // 转换为秒
            response.addCookie(sessionCookie);
            
            return Result.success(loginResponse);
        } catch (BusinessException e) {
            log.warn("登录失败: {}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.error("登录失败，请重试");
        }
    }

    /**
     * 退出登录
     */
    @PostMapping(value = "/logout")
    public Result<Void> logout(@CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId,
                               HttpServletResponse response) {
        authService.logout(sessionId);
        
        // 清除Cookie
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, "");
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);
        
        return Result.success();
    }

    /**
     * 检查会话状态
     */
    @GetMapping("/session")
    public Result<LoginResponse> checkSession(@CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId) {
        if (sessionId == null) {
            return Result.unauthorized();
        }
        
        try {
            LoginResponse session = authService.getSession(sessionId);
            if (session == null) {
                return Result.unauthorized();
            }
            
            return Result.success(session);
        } catch (Exception e) {
            return Result.unauthorized();
        }
    }
    
    /**
     * 检查会话状态（/check路径）
     * 前端使用此接口验证用户登录状态
     */
    @GetMapping("/session/check")
    public Result<LoginResponse> sessionCheck(@CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId) {
        return checkSession(sessionId);
    }
} 