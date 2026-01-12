package com.muxin.gateway.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Admin管理配置类.
 * <p>
 * 该配置类负责管理后台的核心配置，包括：
 * <ul>
 *     <li>启用AdminProperties配置属性类</li>
 *     <li>配置CORS跨域支持，允许前端应用访问API</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminConfig {

    /**
     * 配置CORS跨域支持.
     * <p>
     * 允许所有来源、所有常用HTTP方法、所有请求头，预检请求缓存时间为1小时。
     * </p>
     *
     * @return WebMvcConfigurer配置器实例
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
} 