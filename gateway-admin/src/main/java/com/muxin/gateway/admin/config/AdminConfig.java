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
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminConfig {

    /**
     * 配置CORS跨域支持.
     * <p>
     * 使用allowedOriginPatterns替代allowedOrigins以支持credentials模式。
     * 生产环境应通过AdminProperties配置具体的允许域名。
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
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
} 