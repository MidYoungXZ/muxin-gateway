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
 * Sa-Token权限认证配置类.
 * <p>
 * 该配置类负责集成Sa-Token权限认证框架，采用JWT的无状态模式进行认证。
 * 主要功能包括：
 * <ul>
 *     <li>配置Sa-Token使用JWT无状态模式</li>
 *     <li>注册Sa-Token拦截器，实现登录认证</li>
 *     <li>配置无需认证的路径白名单（登录接口、Swagger文档等）</li>
 * </ul>
 * </p>
 * <p>
 * 注意：该配置类当前被注释掉，未启用。
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 
 * @version 1.0.0
*/
//@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 配置Sa-Token使用JWT无状态模式.
     * <p>
     * 采用Stateless模式，不依赖Session，适合分布式和微服务架构。
     * </p>
     *
     * @return StpLogic实例，使用JWT无状态模式
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }

    /**
     * 注册Sa-Token拦截器.
     * <p>
     * 配置登录认证拦截规则，拦截所有请求，但排除以下路径：
     * <ul>
     *     <li>/admin/api/auth/login - 登录接口</li>
     *     <li>/admin/api/auth/refresh - 令牌刷新接口</li>
     *     <li>/admin/api/test/** - 测试接口</li>
     *     <li>/swagger-ui/** - Swagger UI</li>
     *     <li>/v3/api-docs/** - API文档</li>
     *     <li>/admin/ws/** - WebSocket端点</li>
     * </ul>
     * </p>
     *
     * @param registry 拦截器注册器
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