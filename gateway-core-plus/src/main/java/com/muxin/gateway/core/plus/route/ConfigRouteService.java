package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CONFIG类型路由目标实现
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class ConfigRouteService implements RouteService {

    private final ServiceDefinition serviceDefinition;
    private final List<EndpointAddress> addresses;
    private final Map<String, Object> config;

    public ConfigRouteService(ServiceDefinition serviceDefinition,
                              List<EndpointAddress> addresses,
                              Map<String, Object> config) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition不能为空");
        this.addresses = Objects.requireNonNull(addresses, "addresses不能为空");
        this.config = config;

        if (!serviceDefinition.isConfigType()) {
            throw new IllegalArgumentException("ConfigRouteService只支持CONFIG类型服务");
        }

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置至少一个地址");
        }

        log.info("创建CONFIG路由服务: {} - {} addresses", serviceDefinition.getName(), addresses.size());
    }

    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public List<EndpointAddress> getTargetAddresses() {
        return addresses;
    }

    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }

    @Override
    public EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy strategy) {
        if (addresses == null || addresses.isEmpty()) {
            log.error("[ConfigRouteService] 服务 {} 没有配置地址", serviceDefinition.getName());
            throw new IllegalStateException("服务 " + serviceDefinition.getName() + " 没有配置可用地址");
        }

        if (strategy == null) {
            log.warn("[ConfigRouteService] 负载均衡策略为空，使用第一个地址: {}", addresses.get(0).toUri());
            return addresses.get(0);
        }

        try {
            EndpointAddress selected = strategy.select(addresses, context);
            log.debug("[ConfigRouteService] 选择目标: {} (策略: {}, 服务: {})",
                    selected.toUri(), strategy.getStrategyName(), serviceDefinition.getName());
            return selected;

        } catch (Exception e) {
            log.warn("[ConfigRouteService] 负载均衡选择失败，使用第一个地址作为降级: {}, 错误: {}",
                    addresses.get(0).toUri(), e.getMessage());
            return addresses.get(0);
        }
    }

    @Override
    public void refresh() {
        // CONFIG类型服务不需要刷新
    }

    @Override
    public boolean isHealthy() {
        return addresses != null && !addresses.isEmpty();
    }

    @Override
    public String getServiceId() {
        return serviceDefinition.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigRouteService that = (ConfigRouteService) o;
        return Objects.equals(serviceDefinition.getId(), that.serviceDefinition.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceDefinition.getId());
    }

    @Override
    public String toString() {
        return String.format(
                "ConfigRouteService{serviceId='%s', serviceName='%s', addresses=%d}",
                serviceDefinition.getId(), serviceDefinition.getName(), addresses.size()
        );
    }
}