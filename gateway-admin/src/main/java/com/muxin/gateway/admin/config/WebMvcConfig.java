package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Swagger UI资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.18.2/");

        // Vue3 管理界面静态资源
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/favicon.ico", "/favicon.svg")
                .addResourceLocations("classpath:/static/");

        // Vue3 构建产物
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.gif", "/*.svg", "/*.ico")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径重定向到管理界面
        registry.addRedirectViewController("/", "/index.html");
        // favicon
        registry.addRedirectViewController("/favicon.ico", "/favicon.svg");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 开启尾斜杠匹配
        configurer.setUseTrailingSlashMatch(true);
    }
}