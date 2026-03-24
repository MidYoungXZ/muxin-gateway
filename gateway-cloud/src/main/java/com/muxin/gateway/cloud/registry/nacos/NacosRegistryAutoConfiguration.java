package com.muxin.gateway.cloud.registry.nacos;

import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(name = "com.alibaba.nacos.api.naming.NamingService")
@ConditionalOnProperty(prefix = "muxin.gateway.registry.nacos", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NacosRegistryAutoConfiguration {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    @ConfigurationProperties(prefix = "muxin.gateway.registry.nacos")
    public NacosRegistryProperties nacosRegistryProperties() {
        log.info("[NacosRegistryAutoConfiguration] 创建 Nacos 配置属性");
        return new NacosRegistryProperties();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry nacosServiceRegistry(NacosRegistryProperties properties) {
        log.info("[NacosRegistryAutoConfiguration] 创建 NacosServiceRegistry");

        if (properties.getPort() <= 0) {
            properties.setPort(serverPort);
        }

        NacosServiceRegistry registry = new NacosServiceRegistry(properties);
        registry.init();
        return registry;
    }
}