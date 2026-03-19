package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.ServiceRegistry;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DISCOVERY 类型路由服务实现
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DiscoveryRouteService implements RouteService {

    private final ServiceDefinition serviceDefinition;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, Object> config;

    private volatile List<EndpointAddress> cachedAddresses;
    private volatile long lastRefreshTime;
    private final long cacheExpireTime;
    private final Object refreshLock = new Object();

    public DiscoveryRouteService(ServiceDefinition serviceDefinition,
                                 ServiceRegistry serviceRegistry,
                                 Map<String, Object> config) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition 不能为空");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry 不能为空");
        this.config = config;

        if (!serviceDefinition.isDiscoveryType()) {
            throw new IllegalArgumentException("DiscoveryRouteService 只支持 DISCOVERY 类型服务");
        }

        this.cacheExpireTime = getCacheExpireTime(config);
        this.cachedAddresses = new ArrayList<>();
        this.lastRefreshTime = 0;

        log.info("创建 DISCOVERY 路由服务：{} - 缓存过期时间：{}ms",
                serviceDefinition.getName(), cacheExpireTime);
    }

    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public List<EndpointAddress> getTargetAddresses() {
        if (needsRefresh()) {
            refreshAddresses();
        }
        return new ArrayList<>(cachedAddresses);
    }

    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }

    @Override
    public EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy strategy) {
        List<EndpointAddress> addresses = getTargetAddresses();

        if (addresses == null || addresses.isEmpty()) {
            String errorMsg = String.format("服务 %s 没有可用的实例（服务发现返回空列表）",
                    serviceDefinition.getName());
            log.error("[DiscoveryRouteService] {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (strategy == null) {
            log.warn("[DiscoveryRouteService] 负载均衡策略为空，使用第一个地址：{}",
                    addresses.get(0).toUri());
            return addresses.get(0);
        }

        try {
            EndpointAddress selected = strategy.select(addresses, context);
            log.debug("[DiscoveryRouteService] 选择实例：{} -> {} (策略：{}, 可用实例：{})",
                    serviceDefinition.getName(), selected.toUri(), strategy.getStrategyName(), addresses.size());
            return selected;

        } catch (Exception e) {
            log.warn("[DiscoveryRouteService] 负载均衡选择失败，使用第一个地址作为降级：{}, 错误：{}",
                    addresses.get(0).toUri(), e.getMessage());
            return addresses.get(0);
        }
    }

    @Override
    public void refresh() {
        refreshAddresses();
    }

    @Override
    public boolean isHealthy() {
        List<EndpointAddress> addresses = getTargetAddresses();
        return addresses != null && !addresses.isEmpty();
    }

    @Override
    public String getServiceId() {
        return serviceDefinition.getId();
    }

    private boolean needsRefresh() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastRefreshTime) > cacheExpireTime || cachedAddresses.isEmpty();
    }

    private void refreshAddresses() {
        synchronized (refreshLock) {
            if (!needsRefresh()) {
                return;
            }

            try {
                log.debug("刷新 DISCOVERY 服务地址缓存：{}", serviceDefinition.getName());

                List<ServiceInstance> instances = serviceRegistry.selectInstances(serviceDefinition.getName());
                List<EndpointAddress> newAddresses = convertInstancesToAddresses(instances);

                this.cachedAddresses = newAddresses;
                this.lastRefreshTime = System.currentTimeMillis();

                log.info("DISCOVERY 服务 {} 地址缓存已更新，实例数量：{}", serviceDefinition.getName(), newAddresses.size());

            } catch (Exception e) {
                log.error("刷新 DISCOVERY 服务地址缓存失败：{}", serviceDefinition.getName(), e);
            }
        }
    }

    public CompletableFuture<Void> refreshAddressesAsync() {
        return CompletableFuture.runAsync(() -> refreshAddresses())
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.warn("异步刷新地址缓存失败：{}", serviceDefinition.getName(), throwable);
                    return null;
                });
    }

    private List<EndpointAddress> convertInstancesToAddresses(List<ServiceInstance> instances) {
        List<EndpointAddress> addresses = new ArrayList<>();

        for (ServiceInstance instance : instances) {
            try {
                if (isHealthyInstance(instance)) {
                    EndpointAddress address = instance.getAddress();
                    addresses.add(address);
                    log.debug("转换服务实例：{} -> {}", instance.instanceId(), address.toUri());
                }
            } catch (Exception e) {
                log.warn("转换服务实例失败：{}", instance.instanceId(), e);
            }
        }

        return addresses;
    }

    private boolean isHealthyInstance(ServiceInstance instance) {
        return instance.getStatus().isHealthy();
    }

    private long getCacheExpireTime(Map<String, Object> config) {
        if (config != null) {
            Object cacheTime = config.get("cache-expire-time");
            if (cacheTime instanceof Number) {
                return ((Number) cacheTime).longValue();
            }
            if (cacheTime instanceof String) {
                try {
                    return Long.parseLong((String) cacheTime);
                } catch (NumberFormatException e) {
                    log.warn("无法解析缓存过期时间：{}, 使用默认值 30000ms", cacheTime);
                }
            }
        }
        return 30000L;
    }

    public void forceRefresh() {
        synchronized (refreshLock) {
            this.lastRefreshTime = 0;
            refreshAddresses();
        }
        log.info("强制刷新 DISCOVERY 服务缓存：{}", serviceDefinition.getName());
    }

    public void clearCache() {
        synchronized (refreshLock) {
            this.cachedAddresses.clear();
            this.lastRefreshTime = 0;
        }
        log.info("清空 DISCOVERY 服务缓存：{}", serviceDefinition.getName());
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
                "DiscoveryRouteService{serviceId='%s', serviceName='%s', instances=%d}",
                serviceDefinition.getId(), serviceDefinition.getName(), cachedAddresses.size()
        );
    }
}
