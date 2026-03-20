package com.muxin.gateway.core.route;

import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * DISCOVERY类型路由服务工厂
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
        log.debug("创建DISCOVERY路由服务: {}", serviceDefinition.getId());
        validateConfig(serviceDefinition);
        return new DiscoveryRouteService(serviceDefinition, serviceRegistry);
    }

    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("验证DISCOVERY路由服务定义: {}", serviceDefinition.getId());

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
                if (seconds < 5 || seconds > 3600) {
                    throw new IllegalArgumentException("缓存过期时间必须在5-3600秒之间");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("缓存过期时间必须是数字: " + cacheExpireTime);
            }
        }
    }

    @Override
    public String toString() {
        return "DiscoveryRouteServiceFactory{supportedType=" + getSupportedType() + "}";
    }
}