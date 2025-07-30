package com.muxin.gateway.admin.controller;

import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.LoginDTO;
import com.muxin.gateway.admin.model.vo.LoginVO;
import com.muxin.gateway.admin.model.vo.UserInfoVO;
import com.muxin.gateway.admin.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user-info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    public Result<Void> refreshToken() {
        authService.refreshToken();
        return Result.success();
    }
} 