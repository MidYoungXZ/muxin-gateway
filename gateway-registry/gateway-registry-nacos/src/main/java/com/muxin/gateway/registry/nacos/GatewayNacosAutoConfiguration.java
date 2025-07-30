package com.muxin.gateway.registry.nacos;

import com.muxin.gateway.core.config.GatewayProperties;
import com.muxin.gateway.core.registry.RegisterCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @projectname: muxin-gateway
 * @filename: GatewayNacosAutoConfiguration
 * @author: yangxz
 * @data:2025/6/17 20:23
 * @description:
 */
@Slf4j
@Configuration
public class GatewayNacosAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "muxin.gateway.register.type", havingValue = "nacos", matchIfMissing = true)
    public RegisterCenter nacosRegisterCenter(GatewayProperties gatewayProperties) {
        GatewayProperties.RegisterProperties registerConfig = gatewayProperties.getRegister();
        log.info("Creating Nacos RegisterCenter with address: {}, group: {}, namespace: {}",
                registerConfig.getAddress(), registerConfig.getGroup(), registerConfig.getNamespace());

        // NacosRegisterCenter构造函数参数顺序: address, groupName, clusterName, username, password
        // 这里使用namespace作为clusterName
        return new NacosRegisterCenter(
                registerConfig.getAddress(),
                registerConfig.getGroup(),
                registerConfig.getNamespace(),  // 使用namespace作为clusterName
                registerConfig.getUsername(),
                registerConfig.getPassword()
        );
    }


}
