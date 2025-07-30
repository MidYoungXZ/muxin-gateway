package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;

/**
 * 数据源配置
 * 让Spring Boot自动配置数据源和MyBatis-Flex
 *
 * @author muxin
 */
@Configuration
public class DataSourceConfig {
    // 使用Spring Boot的自动配置，不需要手动创建数据源和SqlSessionFactory
    // @MapperScan 已经在 GatewayAdminAutoConfiguration 中配置
} 