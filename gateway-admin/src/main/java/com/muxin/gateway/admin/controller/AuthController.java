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
 * 认证管理控制器.
 * <p>
 * 该控制器负责处理用户认证相关的HTTP请求，包括：
 * <ul>
 *     <li>用户登录</li>
 *     <li>用户登出</li>
 *     <li>获取当前用户信息</li>
 *     <li>刷新访问令牌</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录.
     * <p>
     * 接收用户登录请求，验证用户名和密码，成功后返回登录信息（包含访问令牌）。
     * </p>
     *
     * @param dto 登录请求数据传输对象，包含用户名和密码
     * @return 包含登录信息（令牌等）的响应结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 用户登出.
     * <p>
     * 处理用户登出请求，清除当前用户的登录状态和令牌信息。
     * </p>
     *
     * @return 空响应结果，表示登出成功
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息.
     * <p>
     * 根据当前请求的令牌获取已登录用户的详细信息。
     * </p>
     *
     * @return 包含用户信息的响应结果
     */
    @GetMapping("/user-info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 刷新访问令牌.
     * <p>
     * 当令牌即将过期时，可以使用此接口刷新访问令牌，延长会话有效期。
     * </p>
     *
     * @return 空响应结果，表示令牌刷新成功
     */
    @PostMapping("/refresh-token")
    public Result<Void> refreshToken() {
        authService.refreshToken();
        return Result.success();
    }
} 