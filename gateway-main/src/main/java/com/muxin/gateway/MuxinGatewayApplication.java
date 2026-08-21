package com.muxin.gateway;

import com.muxin.gateway.admin.GatewayAdminAutoConfiguration;
import com.muxin.gateway.config.GatewayAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@Import({GatewayAdminAutoConfiguration.class, GatewayAutoConfiguration.class})
public class MuxinGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuxinGatewayApplication.class, args);
    }
}
