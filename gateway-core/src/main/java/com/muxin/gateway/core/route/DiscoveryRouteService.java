package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.service.EndpointAddress;
import com.muxin.gateway.core.service.HttpEndpointAddress;
import com.muxin.gateway.core.service.ServiceInstance;
import com.muxin.gateway.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DISCOVERY类型路由服务实现
 * 通过服务注册中心动态发现服务实例
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DiscoveryRouteService implements RouteService {

    private final ServiceDefinition serviceDefinition;
    private final ServiceRegistry serviceRegistry;
    private final long cacheExpireTime;

    public DiscoveryRouteService(ServiceDefinition serviceDefinition, ServiceRegistry serviceRegistry) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition不能为空");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");

        if (!serviceDefinition.isDiscoveryType()) {
            throw new IllegalArgumentException("DiscoveryRouteService只支持DISCOVERY类型服务");
        }

        this.cacheExpireTime = getCacheExpireTime(serviceDefinition.getConfig());
        log.info("创建DISCOVERY路由服务: {} - 缓存过期时间: {}ms", serviceDefinition.getName(), cacheExpireTime);
    }

    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public List<EndpointAddress> getTargetAddresses() {
        List<ServiceInstance> instances = serviceRegistry.getHealthyInstances(serviceDefinition.getName());
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
            String errorMsg = String.format("服务 %s 没有可用实例", serviceDefinition.getName());
            log.error("[DiscoveryRouteService] {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (strategy == null) {
            log.warn("[DiscoveryRouteService] 负载均衡策略为空，使用第一个地址: {}", addresses.get(0).toUri());
            return addresses.get(0);
        }

        try {
            EndpointAddress selected = strategy.select(addresses, context);
            log.debug("[DiscoveryRouteService] 选择实例: {} -> {} (策略: {}, 可用实例: {})",
                    serviceDefinition.getName(), selected.toUri(), strategy.getStrategyName(), addresses.size());
            return selected;
        } catch (Exception e) {
            log.warn("[DiscoveryRouteService] 负载均衡选择失败，使用第一个地址: {}, 错误: {}",
                    addresses.get(0).toUri(), e.getMessage());
            return addresses.get(0);
        }
    }

    @Override
    public void refresh() {
        log.debug("[DiscoveryRouteService] 刷新服务实例: {}", serviceDefinition.getName());
    }

    @Override
    public boolean isHealthy() {
        List<ServiceInstance> instances = serviceRegistry.getHealthyInstances(serviceDefinition.getName());
        return instances != null && !instances.isEmpty();
    }

    @Override
    public String getServiceId() {
        return serviceDefinition.getId();
    }

    private long getCacheExpireTime(Map<String, Object> config) {
        if (config != null) {
            Object cacheTime = config.get("cache-expire-time");
            if (cacheTime instanceof Number) {
                return ((Number) cacheTime).longValue();
            }
        }
        return 30000L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryRouteService that = (DiscoveryRouteService) o;
        return Objects.equals(serviceDefinition.getId(), that.serviceDefinition.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceDefinition.getId());
    }

    @Override
    public String toString() {
        return String.format(
                "DiscoveryRouteService{serviceId='%s', serviceName='%s'}",
                serviceDefinition.getId(), serviceDefinition.getName()
        );
    }
}