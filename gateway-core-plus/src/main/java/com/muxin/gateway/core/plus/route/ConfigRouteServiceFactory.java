package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.HttpEndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * CONFIG类型路由服务工厂
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class ConfigRouteServiceFactory implements RouteServiceFactory {

    @Override
    public ServiceType getSupportedType() {
        return ServiceType.CONFIG;
    }

    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("创建CONFIG类型路由服务: {}", serviceDefinition.getId());

        validateConfig(serviceDefinition);

        List<EndpointAddress> addresses = convertAddresses(serviceDefinition);

        return new ConfigRouteService(
                serviceDefinition,
                addresses,
                serviceDefinition.getConfig()
        );
    }

    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("验证CONFIG类型配置: {}", serviceDefinition.getId());

        if (serviceDefinition.getType() != ServiceType.CONFIG) {
            throw new IllegalArgumentException("服务类型必须是CONFIG");
        }

        validateConfigTypeDefinition(serviceDefinition);
    }

    private void validateConfigTypeDefinition(ServiceDefinition serviceDefinition) {
        if (serviceDefinition.getAddresses() == null || serviceDefinition.getAddresses().isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置addresses");
        }

        for (int i = 0; i < serviceDefinition.getAddresses().size(); i++) {
            AddressDefinition addressDef = serviceDefinition.getAddresses().get(i);
            if (addressDef == null) {
                throw new IllegalArgumentException("addresses[" + i + "]不能为空");
            }

            if (!addressDef.isStaticAddress()) {
                throw new IllegalArgumentException(
                        "CONFIG类型服务只支持静态地址(http://或https://)，当前地址: " + addressDef.getUri()
                );
            }

            try {
                addressDef.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException("addresses[" + i + "]配置无效: " + e.getMessage(), e);
            }
        }
    }

    private List<EndpointAddress> convertAddresses(ServiceDefinition serviceDefinition) {
        List<EndpointAddress> addresses = new ArrayList<>();

        for (AddressDefinition addressDef : serviceDefinition.getAddresses()) {
            try {
                EndpointAddress address = new HttpEndpointAddress(addressDef.getUri());
                addresses.add(address);

                log.debug("转换地址: {} -> {}", addressDef.getUri(), address.toUri());

            } catch (Exception e) {
                log.error("转换地址失败: {}", addressDef.getUri(), e);
                throw new IllegalArgumentException("地址转换失败: " + addressDef.getUri(), e);
            }
        }

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("没有有效的地址配置");
        }

        log.info("CONFIG服务 {} 转换地址完成，共 {} 个地址",
                serviceDefinition.getName(), addresses.size());

        return addresses;
    }

    @Override
    public String toString() {
        return "ConfigRouteServiceFactory{supportedType=" + getSupportedType() + "}";
    }
}
