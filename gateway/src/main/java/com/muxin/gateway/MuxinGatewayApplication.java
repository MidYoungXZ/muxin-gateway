package com.muxin.gateway;

import com.muxin.gateway.admin.GatewayAdminAutoConfiguration;
import com.muxin.gateway.core.plus.GatewayBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 网关应用程序主类
 *
 * 应用程序启动入口，负责初始化系统核心组件和启动服务
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Import(GatewayAdminAutoConfiguration.class)
@Slf4j
public class MuxinGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuxinGatewayApplication.class, args);
    }

    @Bean
    public GatewayBootstrap gatewayBootstrap() {
        return new GatewayBootstrap();
    }
}
