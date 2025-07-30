package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.ServiceRegistry;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DISCOVERY类型路由服务实现
 * 通过服务发现中心动态获取服务实例，负载均衡由外部Route管理
 *
 * @author muxin
 */
@Slf4j
public class DiscoveryRouteService implements RouteService {
    
    // ========== 服务定义 ==========
    private final ServiceDefinition serviceDefinition;
    
    // ========== 协议配置 ==========
    private final Protocol supportProtocol;
    
    // ========== 服务发现相关 ==========
    private final ServiceRegistry serviceRegistry;
    private final Map<String, Object> config;
    
    // ========== 缓存和性能优化 ==========
    private volatile List<EndpointAddress> cachedAddresses;
    private volatile long lastRefreshTime;
    private final Duration cacheExpireTime;
    private final Object refreshLock = new Object();
    
    public DiscoveryRouteService(ServiceDefinition serviceDefinition,
                                 Protocol supportProtocol,
                                 ServiceRegistry serviceRegistry,
                                 Map<String, Object> config) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition不能为空");
        this.supportProtocol = Objects.requireNonNull(supportProtocol, "supportProtocol不能为空");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");
        this.config = config;
        
        // 验证服务类型
        if (!serviceDefinition.isDiscoveryType()) {
            throw new IllegalArgumentException("DiscoveryRouteService只支持DISCOVERY类型服务");
        }
        
        // 缓存过期时间，默认30秒
        this.cacheExpireTime = getCacheExpireTime(config);
        this.cachedAddresses = new ArrayList<>();
        this.lastRefreshTime = 0;
        
        log.info("创建DISCOVERY路由服务: {} - 缓存过期时间: {}秒", 
                serviceDefinition.getName(), cacheExpireTime.getSeconds());
    }

    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public Protocol supportProtocol() {
        return supportProtocol;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        // 检查缓存是否需要刷新
        if (needsRefresh()) {
            refreshAddresses();
        }
        
        return new ArrayList<>(cachedAddresses); // 返回副本避免并发修改
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy strategy) {
        try {
            // 获取最新的地址列表
            List<EndpointAddress> addresses = getTargetAddresses();
            
            if (addresses.isEmpty()) {
                throw new IllegalStateException("服务 " + serviceDefinition.getName() + " 没有可用的实例");
            }
            
            // 使用外部提供的负载均衡策略选择地址
            EndpointAddress selected = strategy.select(addresses, context);
            
            log.debug("选择服务实例: {} -> {} (策略: {}, 可用实例: {})", 
                    serviceDefinition.getName(), selected.toUri(), strategy.getStrategyName(), addresses.size());
            
            return selected;
            
        } catch (Exception e) {
            log.error("服务发现选择地址失败: {}", serviceDefinition.getName(), e);
            throw new RuntimeException("服务发现选择地址失败: " + serviceDefinition.getName(), e);
        }
    }
    
    /**
     * 检查缓存是否需要刷新
     */
    private boolean needsRefresh() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastRefreshTime) > cacheExpireTime.toMillis() || cachedAddresses.isEmpty();
    }
    
    /**
     * 刷新地址缓存
     */
    private void refreshAddresses() {
        synchronized (refreshLock) {
            // 双重检查锁定
            if (!needsRefresh()) {
                return;
            }
            
            try {
                log.debug("刷新DISCOVERY服务地址缓存: {}", serviceDefinition.getName());
                
                // 从服务发现中心获取实例
                List<ServiceInstance> instances = serviceRegistry.selectInstances(serviceDefinition.getName());
                List<EndpointAddress> newAddresses = convertInstancesToAddresses(instances);
                
                // 更新缓存
                this.cachedAddresses = newAddresses;
                this.lastRefreshTime = System.currentTimeMillis();
                
                log.info("DISCOVERY服务 {} 地址缓存已更新，实例数量: {}", serviceDefinition.getName(), newAddresses.size());
                
            } catch (Exception e) {
                log.error("刷新DISCOVERY服务地址缓存失败: {}", serviceDefinition.getName(), e);
                // 刷新失败时保持原有缓存，避免服务中断
            }
        }
    }
    
    /**
     * 异步刷新地址缓存
     */
    public CompletableFuture<Void> refreshAddressesAsync() {
        return CompletableFuture.runAsync(() -> refreshAddresses())
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.warn("异步刷新地址缓存失败: {}", serviceDefinition.getName(), throwable);
                    return null;
                });
    }
    
    /**
     * 将服务实例转换为端点地址
     */
    private List<EndpointAddress> convertInstancesToAddresses(List<ServiceInstance> instances) {
        List<EndpointAddress> addresses = new ArrayList<>();
        
        for (ServiceInstance instance : instances) {
            try {
                // 只转换健康的实例
                if (isHealthyInstance(instance)) {
                    EndpointAddress address = instance.getAddress();
                    addresses.add(address);
                    
                    log.debug("转换服务实例: {} -> {}", instance.instanceId(), address.toUri());
                }
            } catch (Exception e) {
                log.warn("转换服务实例失败: {}", instance.instanceId(), e);
            }
        }
        
        return addresses;
    }
    
    /**
     * 检查实例是否健康
     */
    private boolean isHealthyInstance(ServiceInstance instance) {
        // 可以根据实例状态、健康检查结果等判断
        return instance.getStatus().isHealthy();
    }
    
    /**
     * 获取缓存过期时间
     */
    private Duration getCacheExpireTime(Map<String, Object> config) {
        if (config != null) {
            Object cacheTime = config.get("cache-expire-time");
            if (cacheTime instanceof Number) {
                return Duration.ofSeconds(((Number) cacheTime).longValue());
            }
            if (cacheTime instanceof String) {
                try {
                    return Duration.ofSeconds(Long.parseLong((String) cacheTime));
                } catch (NumberFormatException e) {
                    log.warn("无法解析缓存过期时间: {}, 使用默认值30秒", cacheTime);
                }
            }
        }
        return Duration.ofSeconds(30); // 默认30秒
    }
    
    // ========== Getter方法 ==========
    
    public String getServiceId() {
        return serviceDefinition.getId();
    }
    
    public String getServiceName() {
        return serviceDefinition.getName();
    }
    
    public ServiceType getServiceType() {
        return serviceDefinition.getType();
    }
    
    public ServiceRegistry getServiceDiscovery() {
        return serviceRegistry;
    }
    
    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        long cacheAge = System.currentTimeMillis() - lastRefreshTime;
        return String.format("缓存年龄: %dms, 地址数量: %d, 过期时间: %dms", 
                cacheAge, cachedAddresses.size(), cacheExpireTime.toMillis());
    }
    
    /**
     * 强制刷新缓存
     */
    public void forceRefresh() {
        synchronized (refreshLock) {
            this.lastRefreshTime = 0; // 强制过期
            refreshAddresses();
        }
        log.info("强制刷新DISCOVERY服务缓存: {}", serviceDefinition.getName());
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        synchronized (refreshLock) {
            this.cachedAddresses.clear();
            this.lastRefreshTime = 0;
        }
        log.info("清空DISCOVERY服务缓存: {}", serviceDefinition.getName());
    }
    
    /**
     * 获取负载均衡统计信息
     */
        public String getLoadBalanceStats() {
        return "统计功能暂未实现";
    }
    
    /**
     * 重置负载均衡状态
     */
    public void resetLoadBalanceState() {
        // This method is no longer needed as LoadBalanceStrategy is external
        log.info("重置DISCOVERY路由目标负载均衡状态: {}", serviceDefinition.getName());
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
            "DiscoveryRouteTarget{serviceId='%s', serviceName='%s', protocol=%s, instances=%d, strategy='%s', cache='%s'}",
            serviceDefinition.getId(), serviceDefinition.getName(), supportProtocol.type(), cachedAddresses.size(),
            "External", getCacheStats()
        );
    }
} 