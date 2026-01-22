package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Spring MVC配置类.
 * <p>
 * 该配置类负责Spring MVC的相关配置，包括：
 * <ul>
 *     <li>静态资源处理和缓存控制</li>
 *     <li>视图控制器和重定向规则</li>
 *     <li>路径匹配策略</li>
 * </ul>
 * </p>
 * <p>
 * 主要用于支持Vue3管理界面的静态资源访问和Swagger UI的展示。
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器.
     * <p>
     * 配置各种静态资源的访问路径映射和缓存策略，包括：
     * <ul>
     *     <li>Swagger UI资源</li>
     *     <li>Vue3管理界面的静态资源（HTML、CSS、JS、图片等）</li>
     *     <li>传统静态资源目录</li>
     * </ul>
     * </p>
     * <p>
     * 注意：不要配置/**模式，避免与API路径冲突。
     * </p>
     *
     * @param registry 资源处理器注册器
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
     * 添加视图控制器.
     * <p>
     * 配置URL重定向规则，将特定的访问路径重定向到目标页面：
     * <ul>
     *     <li>/admin 和 /admin/ 重定向到 /admin/index.html</li>
     *     <li>根路径重定向到 /index.html</li>
     *     <li>favicon.ico重定向到favicon.svg</li>
     * </ul>
     * </p>
     *
     * @param registry 视图控制器注册器
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
     * 配置路径匹配策略.
     * <p>
     * 不添加任何前缀，保持所有API接口的原有路径。
     * 这样更符合RESTful API的设计原则。
     * </p>
     *
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 移除自动添加/admin前缀的配置，让所有Controller保持原有的@RequestMapping路径
        // 如果需要管理界面相关的路径，可以在Controller中直接定义
    }
} 