package com.muxin.gateway.cloud.discovery.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.muxin.gateway.cloud.discovery.CloudProperties;
import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
@ConditionalOnClass({NamingService.class, DiscoveryClient.class})
@ConditionalOnProperty(prefix = "muxin.gateway.cloud", name = "discovery", havingValue = "nacos", matchIfMissing = true)
@EnableConfigurationProperties(CloudProperties.class)
public class NacosRegistryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(NamingService.class)
    public NamingService namingService(NacosDiscoveryProperties discoveryProperties) {
        log.info("[NacosRegistryAutoConfiguration] 创建 NamingService");
        log.info("[NacosRegistryAutoConfiguration] Nacos 服务地址: {}", discoveryProperties.getServerAddr());
        log.info("[NacosRegistryAutoConfiguration] 命名空间: {}", discoveryProperties.getNamespace());

        Properties properties = new Properties();
        properties.setProperty("serverAddr", discoveryProperties.getServerAddr());
        
        if (discoveryProperties.getNamespace() != null && !discoveryProperties.getNamespace().isEmpty()) {
            properties.setProperty("namespace", discoveryProperties.getNamespace());
        }
        if (discoveryProperties.getUsername() != null && !discoveryProperties.getUsername().isEmpty()) {
            properties.setProperty("username", discoveryProperties.getUsername());
        }
        if (discoveryProperties.getPassword() != null && !discoveryProperties.getPassword().isEmpty()) {
            properties.setProperty("password", discoveryProperties.getPassword());
        }
        if (discoveryProperties.getAccessKey() != null && !discoveryProperties.getAccessKey().isEmpty()) {
            properties.setProperty("accessKey", discoveryProperties.getAccessKey());
        }
        if (discoveryProperties.getSecretKey() != null && !discoveryProperties.getSecretKey().isEmpty()) {
            properties.setProperty("secretKey", discoveryProperties.getSecretKey());
        }
        if (discoveryProperties.getClusterName() != null && !discoveryProperties.getClusterName().isEmpty()) {
            properties.setProperty("clusterName", discoveryProperties.getClusterName());
        }
        if (discoveryProperties.getGroup() != null && !discoveryProperties.getGroup().isEmpty()) {
            properties.setProperty("groupName", discoveryProperties.getGroup());
        }

        try {
            return NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("[NacosRegistryAutoConfiguration] 创建 NamingService 失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建 NamingService 失败", e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry serviceRegistry(DiscoveryClient discoveryClient, NamingService namingService, NacosDiscoveryProperties discoveryProperties) {
        log.info("[NacosRegistryAutoConfiguration] 创建 NacosServiceRegistryAdapter");
        log.info("[NacosRegistryAutoConfiguration] 分组: {}", discoveryProperties.getGroup());

        NacosServiceRegistryAdapter adapter = new NacosServiceRegistryAdapter(
                discoveryClient,
                namingService,
                discoveryProperties.getGroup()
        );
        adapter.init();
        adapter.start();
        return adapter;
    }
}