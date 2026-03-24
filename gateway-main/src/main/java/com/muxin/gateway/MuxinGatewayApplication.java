package com.muxin.gateway;

import com.muxin.gateway.admin.GatewayAdminAutoConfiguration;
import com.muxin.gateway.config.GatewayAutoConfiguration;
import com.muxin.gateway.config.YamlProviderAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@Import({GatewayAdminAutoConfiguration.class, GatewayAutoConfiguration.class, YamlProviderAutoConfiguration.class})
public class MuxinGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuxinGatewayApplication.class, args);
    }
}