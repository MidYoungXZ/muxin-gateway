package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.service.DefaultServiceInstance;
import com.muxin.gateway.core.service.EndpointAddress;
import com.muxin.gateway.core.service.HttpEndpointAddress;
import com.muxin.gateway.core.service.ServiceInstance;
import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * STATIC类型路由服务实现
 * 使用配置文件中定义的静态地址列表
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class StaticRouteService implements RouteService {

    private final ServiceDefinition serviceDefinition;
    private final ServiceRegistry serviceRegistry;
    private final List<String> registeredInstanceIds = new ArrayList<>();

    public StaticRouteService(ServiceDefinition serviceDefinition, ServiceRegistry serviceRegistry) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition不能为空");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");

        if (!serviceDefinition.isStaticType()) {
            throw new IllegalArgumentException("StaticRouteService只支持STATIC类型服务");
        }

        log.info("创建STATIC路由服务: {} - {} addresses", 
                serviceDefinition.getName(), serviceDefinition.getAddresses().size());
    }

    private void registerStaticInstances() {
        for (AddressDefinition addrDef : serviceDefinition.getAddresses()) {
            if (addrDef.isStaticAddress()) {
                HttpEndpointAddress address = new HttpEndpointAddress(addrDef.getUri());
                ServiceInstance instance = DefaultServiceInstance
                        .createStatic(
                                serviceDefinition.getId(),
                                address.getHost(),
                                address.getPort(),
                                address.getScheme(),
                                addrDef.getWeight() != null ? addrDef.getWeight() : 100
                        );
                serviceRegistry.registerInstance(instance);
                registeredInstanceIds.add(instance.getInstanceId());
            }
        }
    }

    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public List<EndpointAddress> getTargetAddresses() {
        List<ServiceInstance> instances = serviceRegistry.getHealthyInstances(serviceDefinition.getId());
        return instances.stream()
                .map(inst -> new HttpEndpointAddress(inst.getHost(), inst.getPort(), inst.getScheme()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTargetConfig() {
        return serviceDefinition.getConfig();
    }

    @Override
    public EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy strategy) {
        List<EndpointAddress> addresses = getTargetAddresses();

        if (addresses == null || addresses.isEmpty()) {
            log.error("[StaticRouteService] 服务 {} 没有可用实例", serviceDefinition.getName());
            throw new IllegalStateException("服务 " + serviceDefinition.getName() + " 没有可用实例");
        }

        if (strategy == null) {
            log.warn("[StaticRouteService] 负载均衡策略为空，使用第一个地址: {}", addresses.get(0).toUri());
            return addresses.get(0);
        }

        try {
            EndpointAddress selected = strategy.select(addresses, context);
            log.debug("[StaticRouteService] 选择目标: {} (策略: {}, 服务: {})",
                    selected.toUri(), strategy.getStrategyName(), serviceDefinition.getName());
            return selected;
        } catch (Exception e) {
            log.warn("[StaticRouteService] 负载均衡选择失败，使用第一个地址: {}, 错误: {}",
                    addresses.get(0).toUri(), e.getMessage());
            return addresses.get(0);
        }
    }

    @Override
    public void refresh() {
        for (String instanceId : registeredInstanceIds) {
            serviceRegistry.deregisterInstance(serviceDefinition.getId(), instanceId);
        }
        registeredInstanceIds.clear();
        registerStaticInstances();
    }

    public List<String> registeredInstanceIds() {
        return List.copyOf(registeredInstanceIds);
    }

    @Override
    public boolean isHealthy() {
        List<ServiceInstance> instances = serviceRegistry.getHealthyInstances(serviceDefinition.getId());
        return instances != null && !instances.isEmpty();
    }

    @Override
    public String getServiceId() {
        return serviceDefinition.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticRouteService that = (StaticRouteService) o;
        return Objects.equals(serviceDefinition.getId(), that.serviceDefinition.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceDefinition.getId());
    }

    @Override
    public String toString() {
        return String.format(
                "StaticRouteService{serviceId='%s', serviceName='%s'}",
                serviceDefinition.getId(), serviceDefinition.getName()
        );
    }
}
