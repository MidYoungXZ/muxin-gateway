package com.muxin.gateway.core.listener;

import com.muxin.gateway.core.config.GatewayProperties;
import com.muxin.gateway.core.registry.DefaultServiceInstance;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关服务注册器
 * 在应用启动完成后将网关本身注册到Nacos
 */
@Slf4j
@Component
public class GatewayServiceRegistry implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RegisterCenter registerCenter;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Value("${spring.application.name:muxin-gateway}")
    private String serviceName;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        GatewayProperties.GatewayRegisterProperties registerConfig = gatewayProperties.getRegister().getGateway();

        if (!registerConfig.isEnabled()) {
            log.info("Gateway service registry is disabled");
            return;
        }

        try {
            // 注册网关服务到Nacos
            ServiceInstance gatewayInstance = createGatewayServiceInstance();
            registerCenter.register(gatewayInstance);

            log.info("Gateway service registered successfully: {}", gatewayInstance.getInstanceId());
            log.info("Service details - Name: {}, Host: {}, Port: {}, Weight: {}",
                    getServiceName(), gatewayInstance.getHost(), gatewayInstance.getPort(), gatewayInstance.getWeight());

            // 添加关闭钩子，在应用关闭时注销服务
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Deregistering gateway service...");
                    registerCenter.deregister(gatewayInstance);
                    log.info("Gateway service deregistered successfully");
                } catch (Exception e) {
                    log.error("Failed to deregister gateway service", e);
                }
            }));

        } catch (Exception e) {
            log.error("Failed to register gateway service to Nacos", e);
            // 可以根据业务需求决定是否抛出异常导致启动失败
            // throw new RuntimeException("Gateway service registration failed", e);
        }
    }

    private ServiceInstance createGatewayServiceInstance() throws Exception {
        GatewayProperties.GatewayRegisterProperties registerConfig = gatewayProperties.getRegister().getGateway();

        // 获取本机IP地址
        String host = InetAddress.getLocalHost().getHostAddress();
        int port = gatewayProperties.getNetty().getServer().getPort();

        // 创建服务定义
        DefaultServiceInstance.DefaultServiceDefinition serviceDefinition = new DefaultServiceInstance.DefaultServiceDefinition();
        serviceDefinition.setServiceId(getServiceName());
        serviceDefinition.setScheme(registerConfig.isSecure() ? "https" : "http");
        serviceDefinition.setVersion(registerConfig.getVersion());
        serviceDefinition.setScope("public");
        serviceDefinition.setDescription(registerConfig.getDescription());
        serviceDefinition.setEnabled(true);

        // 创建元数据
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", registerConfig.getVersion());
        metadata.put("description", registerConfig.getDescription());
        metadata.put("service-type", "gateway");
        metadata.put("netty-server", "true");
        metadata.put("status", registerConfig.getStatus());
        metadata.put("health-check-url", registerConfig.getHealthCheckUrl());
        metadata.put("health-check-interval", String.valueOf(registerConfig.getHealthCheckInterval()));
        metadata.put("startup-time", String.valueOf(System.currentTimeMillis()));

        // 添加用户自定义的元数据
        if (registerConfig.getMetadata() != null && !registerConfig.getMetadata().isEmpty()) {
            metadata.putAll(registerConfig.getMetadata());
        }

        // 创建服务实例
        DefaultServiceInstance instance = new DefaultServiceInstance();
        instance.setServiceDefinition(serviceDefinition);
        instance.setInstanceId(generateInstanceId(host, port));
        instance.setHost(host);
        instance.setPort(port);
        instance.setSecure(registerConfig.isSecure());
        instance.setUri(URI.create((registerConfig.isSecure() ? "https" : "http") + "://" + host + ":" + port));
        instance.setWeight(registerConfig.getWeight());
        instance.setHealthy("UP".equalsIgnoreCase(registerConfig.getStatus()));
        instance.setMetadata(metadata);

        return instance;
    }

    private String generateInstanceId(String host, int port) {
        return getServiceName() + "-" + host + "-" + port + "-" + System.currentTimeMillis();
    }

    private String getServiceName() {
        GatewayProperties.GatewayRegisterProperties registerConfig = gatewayProperties.getRegister().getGateway();
        return StringUtils.hasText(registerConfig.getServiceId()) ? registerConfig.getServiceId() : serviceName;
    }
} 