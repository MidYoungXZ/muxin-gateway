package com.muxin.gateway.registry.nacos;

import com.muxin.gateway.core.plus.registry.RegisterCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos注册中心自动配置
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class GatewayNacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "muxin.gateway.register.type", havingValue = "nacos", matchIfMissing = true)
    public RegisterCenter nacosRegisterCenter(NacosProperties nacosProperties) {
        log.info("Creating Nacos RegisterCenter with address: {}, group: {}, namespace: {}",
                nacosProperties.getAddress(), nacosProperties.getGroup(), nacosProperties.getNamespace());

        return new NacosRegisterCenter(
                nacosProperties.getAddress(),
                nacosProperties.getGroup(),
                nacosProperties.getNamespace(),  // 使用namespace作为clusterName
                nacosProperties.getUsername(),
                nacosProperties.getPassword()
        );
    }

    @Bean
    @ConfigurationProperties(prefix = "muxin.gateway.register")
    public NacosProperties nacosProperties() {
        return new NacosProperties();
    }

    /**
     * Nacos配置属性
     */
    @lombok.Data
    public static class NacosProperties {
        private String type = "nacos";
        private String address = "localhost:8848";
        private String username;
        private String password;
        private String group = "DEFAULT_GROUP";
        private String namespace = "DEFAULT";
    }
}
