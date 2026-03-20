package com.muxin.gateway.core.route;

import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * STATIC类型路由服务工厂
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class StaticRouteServiceFactory implements RouteServiceFactory {

    private final ServiceRegistry serviceRegistry;

    public StaticRouteServiceFactory(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");
    }

    @Override
    public ServiceType getSupportedType() {
        return ServiceType.STATIC;
    }

    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("创建STATIC类型路由服务: {}", serviceDefinition.getId());
        validateConfig(serviceDefinition);
        return new StaticRouteService(serviceDefinition, serviceRegistry);
    }

    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("验证STATIC类型配置: {}", serviceDefinition.getId());

        if (serviceDefinition.getType() != ServiceType.STATIC) {
            throw new IllegalArgumentException("服务类型必须是STATIC");
        }

        if (serviceDefinition.getAddresses() == null || serviceDefinition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("STATIC类型服务必须配置addresses");
        }

        for (int i = 0; i < serviceDefinition.getAddresses().size(); i++) {
            AddressDefinition addressDef = serviceDefinition.getAddresses().get(i);
            if (addressDef == null) {
                throw new IllegalArgumentException("addresses[" + i + "]不能为空");
            }

            if (!addressDef.isStaticAddress()) {
                throw new IllegalArgumentException(
                        "STATIC类型服务只支持静态地址(http://或https://)，当前地址: " + addressDef.getUri()
                );
            }

            try {
                addressDef.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException("addresses[" + i + "]配置无效: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        return "StaticRouteServiceFactory{supportedType=" + getSupportedType() + "}";
    }
}