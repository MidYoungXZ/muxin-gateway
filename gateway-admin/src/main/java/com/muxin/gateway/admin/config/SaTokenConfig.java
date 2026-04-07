package com.muxin.gateway.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch("/api/auth/login")
                    .notMatch("/api/auth/refresh-token")
                    .notMatch("/api/test/**")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**")
          .excludePathPatterns(
                "/",
                "/index.html",
                "/favicon.ico",
                "/favicon.svg",
                "/logo.svg",
                "/static/**",
                "/assets/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/ws/**",
                "/error"
          );
    }
}