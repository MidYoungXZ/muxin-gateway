package com.muxin.gateway.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Gateway Admin自动配置类.
 * <p>
 * 该配置类负责Spring Boot应用的自动装配，包括：
 * <ul>
 *     <li>启用定时任务调度支持</li>
 *     <li>配置组件扫描路径，扫描com.muxin.gateway.admin包及其子包下的所有组件</li>
 *     <li>配置MyBatis Mapper扫描路径，扫描mapper包下的所有Mapper接口</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @since 1.0.0
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