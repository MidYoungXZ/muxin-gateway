package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 * 用于配置静态资源处理和视图控制器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理根路径下的静态资源（如favicon.ico）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        
        // 针对/admin路径的资源特殊处理
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/");
        
        // Swagger UI资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.18.2/");
    }

    /**
     * 添加视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 当访问/admin时重定向到/admin/index.html
        registry.addRedirectViewController("/admin", "/admin/index.html");
        // 根路径重定向到/admin/index.html
        registry.addRedirectViewController("/", "/admin/index.html");
        
        // 兼容前端API请求路径
        // 注意：这种方式只能处理GET请求的重定向，对于POST等请求无效
        registry.addRedirectViewController("/api/login", "/admin/api/login");
        registry.addRedirectViewController("/api/logout", "/admin/api/logout");
        registry.addRedirectViewController("/api/session", "/admin/api/session");
        registry.addRedirectViewController("/api/routes", "/admin/api/routes");
    }
} 