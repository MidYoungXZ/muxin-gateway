package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;

/**
 * 数据源配置类.
 * <p>
 * 该配置类利用Spring Boot的自动配置机制来配置数据源和MyBatis-Flex。
 * 使用Spring Boot自动配置可以简化配置，无需手动创建DataSource和SqlSessionFactory。
 * </p>
 * <p>
 * 注意：@MapperScan注解已在GatewayAdminAutoConfiguration中配置，此处无需重复配置。
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * @see com.mybatisflex.spring.boot.MyBatisFlexAutoConfiguration
 */
@Configuration
public class DataSourceConfig {
    // 使用Spring Boot的自动配置，不需要手动创建数据源和SqlSessionFactory
    // @MapperScan 已经在 GatewayAdminAutoConfiguration 中配置
} 