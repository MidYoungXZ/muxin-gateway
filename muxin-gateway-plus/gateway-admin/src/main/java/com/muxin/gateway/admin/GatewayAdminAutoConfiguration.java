package com.muxin.gateway.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Gateway Admin 自动配置类
 * 让Spring Boot自动扫描并装配所有的组件
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.muxin.gateway.admin")
@MapperScan(basePackages = "com.muxin.gateway.admin.mapper")
public class GatewayAdminAutoConfiguration {

    // TODO: 实现基于数据库的路由定义仓库
    // @Bean
    // @Primary
    // public RouteDefinitionRepository dbRouteDefinitionRepository(GatewayProperties gatewayProperties,
    //                                                              AdminProperties adminProperties,
    //                                                              GatewayRouteService gatewayRouteService) {
    //     return new DbRouteDefinitionRepository(gatewayProperties, adminProperties, gatewayRouteService);
    // }


} 