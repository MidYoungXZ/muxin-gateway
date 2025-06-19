package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Spring MVC配置类
 * 用于配置静态资源处理和视图控制器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     * 注意：不要配置/**模式，避免与API路径冲突
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // Swagger UI资源（优先级最高）
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.18.2/");
        
        // Vue3管理界面的静态资源
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/favicon.ico", "/favicon.svg")
                .addResourceLocations("classpath:/static/");
        
        // Vue3构建产物的静态资源
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        
        // 传统静态资源目录
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        
        // 特殊资源文件
        registry.addResourceHandler("/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.gif", "/*.svg", "/*.ico")
                .addResourceLocations("classpath:/static/");

    }

    /**
     * 添加视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 当访问/admin时重定向到/admin/index.html
        registry.addRedirectViewController("/admin", "/admin/index.html");
        // 当访问/admin/时重定向到/admin/index.html
        registry.addRedirectViewController("/admin/", "/admin/index.html");
        // 根路径重定向到主页面
        registry.addRedirectViewController("/", "/index.html");
        // favicon.ico重定向到favicon.svg
        registry.addRedirectViewController("/favicon.ico", "/favicon.svg");
    }
    
    /**
     * 配置路径匹配
     * 不添加任何前缀，保持所有API接口的原有路径
     * 这样更符合RESTful API的设计原则
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 移除自动添加/admin前缀的配置，让所有Controller保持原有的@RequestMapping路径
        // 如果需要管理界面相关的路径，可以在Controller中直接定义
    }
} 