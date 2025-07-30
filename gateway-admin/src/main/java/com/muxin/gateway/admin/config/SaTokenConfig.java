package com.muxin.gateway.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置
 *
 * @author muxin
 */
//@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * Sa-Token 整合 jwt (Stateless 无状态模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }

    /**
     * 注册Sa-Token拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        // 注册Sa-Token的拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录认证 -- 拦截所有路由，并排除/auth/**
            SaRouter.match("/**")
                    .notMatch("/admin/api/auth/login")
                    .notMatch("/admin/api/auth/refresh")
                    .notMatch("/admin/api/test/**")  // 测试路径
                    .notMatch("/swagger-ui/**")
                    .notMatch("/v3/api-docs/**")
                    .notMatch("/admin/ws/**")  // WebSocket路径
                    .check(r -> StpUtil.checkLogin());

            // 角色认证 -- 暂时关闭角色认证
            // SaRouter.match("/admin/api/system/user/**", r -> StpUtil.checkRole("ADMIN"));
            // SaRouter.match("/admin/api/system/role/**", r -> StpUtil.checkRole("ADMIN"));

            // 权限认证 -- 暂时关闭权限认证
            // SaRouter.match("/admin/api/routes", r -> StpUtil.checkPermission("route:list"))
            //         .match("/admin/api/routes/create", r -> StpUtil.checkPermission("route:create"))
            //         .match("/admin/api/routes/update", r -> StpUtil.checkPermission("route:update"))
            //         .match("/admin/api/routes/delete", r -> StpUtil.checkPermission("route:delete"));
        })).addPathPatterns("/**");
    }
} 