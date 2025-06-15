package com.muxin.gateway.admin;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Admin 自动配置类
 * 让Spring Boot自动扫描并装配所有的组件
 */
@Configuration
@ComponentScan("com.muxin.gateway.admin")
public class GatewayAdminAutoConfiguration {
    // 自动装配的配置类，用于在主应用中引入
} 