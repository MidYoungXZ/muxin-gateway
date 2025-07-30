package com.muxin.gateway.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * Gateway Admin 测试应用启动类
 *
 * @author muxin
 */
@SpringBootApplication
@MapperScan(basePackages = "com.muxin.gateway.admin.mapper")
public class GatewayAdminTestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayAdminTestApplication.class, args);
    }
} 