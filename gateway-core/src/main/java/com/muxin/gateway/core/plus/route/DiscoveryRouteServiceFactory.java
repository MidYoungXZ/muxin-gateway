package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * DISCOVERY类型路由服务工厂
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DiscoveryRouteServiceFactory implements RouteServiceFactory {

    private final ServiceRegistry serviceRegistry;

    public DiscoveryRouteServiceFactory(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");
    }

    @Override
    public ServiceType getSupportedType() {
        return ServiceType.DISCOVERY;
    }

    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("开始创建DISCOVERY路由服务: {}", serviceDefinition.getId());

        validateConfig(serviceDefinition);

        try {
            DiscoveryRouteService routeService = new DiscoveryRouteService(
                    serviceDefinition,
                    serviceRegistry,
                    serviceDefinition.getConfig()
            );

            log.info("成功创建DISCOVERY路由服务: {} ({})",
                    serviceDefinition.getName(), serviceDefinition.getId());

            return routeService;

        } catch (Exception e) {
            log.error("创建DISCOVERY路由服务失败: {}", serviceDefinition.getId(), e);
            throw new IllegalArgumentException("创建DISCOVERY路由服务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("开始验证DISCOVERY路由服务定义: {}", serviceDefinition.getId());

        if (serviceDefinition.getType() != ServiceType.DISCOVERY) {
            throw new IllegalArgumentException("服务类型必须是DISCOVERY");
        }

        if (serviceDefinition.getId() == null || serviceDefinition.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceId");
        }

        if (serviceDefinition.getName() == null || serviceDefinition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceName");
        }

        validateDiscoveryConfig(serviceDefinition);

        log.debug("DISCOVERY路由服务定义验证通过: {}", serviceDefinition.getId());
    }

    private void validateDiscoveryConfig(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        if (config == null) {
            return;
        }

        Object cacheExpireTime = config.get("cache-expire-time");
        if (cacheExpireTime != null) {
            try {
                int seconds = Integer.parseInt(cacheExpireTime.toString());
                if (seconds < 5) {
                    throw new IllegalArgumentException("缓存过期时间不能少于5秒");
                }
                if (seconds > 3600) {
                    throw new IllegalArgumentException("缓存过期时间不能超过3600秒");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("缓存过期时间必须是数字: " + cacheExpireTime);
            }
        }

        validateHealthCheckConfig(config);
        validateServiceDiscoveryConfig(config);
    }

    private void validateHealthCheckConfig(Map<String, Object> config) {
        Object healthCheck = config.get("health-check");
        if (healthCheck instanceof Map<?, ?>) {
            Map<?, ?> healthConfig = (Map<?, ?>) healthCheck;

            Object interval = healthConfig.get("interval");
            if (interval != null) {
                try {
                    int seconds = Integer.parseInt(interval.toString());
                    if (seconds < 5 || seconds > 300) {
                        throw new IllegalArgumentException("健康检查间隔必须在5-300秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查间隔必须是数字: " + interval);
                }
            }

            Object timeout = healthConfig.get("timeout");
            if (timeout != null) {
                try {
                    int seconds = Integer.parseInt(timeout.toString());
                    if (seconds < 1 || seconds > 30) {
                        throw new IllegalArgumentException("健康检查超时时间必须在1-30秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查超时时间必须是数字: " + timeout);
                }
            }
        }
    }

    private void validateServiceDiscoveryConfig(Map<String, Object> config) {
        Object registry = config.get("registry");
        if (registry instanceof Map<?, ?>) {
            Map<?, ?> registryConfig = (Map<?, ?>) registry;

            Object type = registryConfig.get("type");
            if (type != null && !isValidRegistryType(type.toString())) {
                throw new IllegalArgumentException("不支持的注册中心类型: " + type);
            }

            Object address = registryConfig.get("address");
            if (address != null && address.toString().trim().isEmpty()) {
                throw new IllegalArgumentException("注册中心地址不能为空");
            }
        }

        Object namespace = config.get("namespace");
        if (namespace != null && namespace.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("命名空间不能为空字符串");
        }
    }

    private boolean isValidRegistryType(String type) {
        return "nacos".equalsIgnoreCase(type) ||
                "eureka".equalsIgnoreCase(type) ||
                "consul".equalsIgnoreCase(type) ||
                "zookeeper".equalsIgnoreCase(type);
    }

    @Override
    public String toString() {
        return "DiscoveryRouteServiceFactory{supportedType=" + getSupportedType() +
                ", serviceRegistry=" + serviceRegistry.getClass().getSimpleName() + "}";
    }
}
