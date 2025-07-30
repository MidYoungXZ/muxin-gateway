package com.muxin.gateway.core.plus.route.service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 默认实例管理器实现
 * 负责服务实例的生命周期管理
 * 
 * @author muxin
 */
@Slf4j
public class DefaultInstanceManager implements InstanceManager {

    // 实例存储：instanceId -> ServiceInstance
    private final Map<String, ServiceInstance> instanceStorage = new ConcurrentHashMap<>();
    
    // 服务分组索引：serviceId -> Set<instanceId>
    private final Map<String, Set<String>> serviceIndex = new ConcurrentHashMap<>();
    
    // 健康实例索引：serviceId -> Set<instanceId>
    private final Map<String, Set<String>> healthyIndex = new ConcurrentHashMap<>();
    
    // 性能统计
    private final AtomicLong totalInstances = new AtomicLong(0);
    private final AtomicLong healthyInstances = new AtomicLong(0);
    
    // 生命周期状态
    private volatile boolean initialized = false;
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    @Override
    public void init() {
        if (!initialized) {
            log.info("[DefaultInstanceManager] 实例管理器初始化");
            initialized = true;
        }
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        if (!initialized) {
            throw new IllegalStateException("实例管理器未初始化");
        }
        log.info("[DefaultInstanceManager] 实例管理器启动");
        started = true;
    }

    @Override
    public void shutdown() {
        if (!shutdown) {
            log.info("[DefaultInstanceManager] 实例管理器关闭");
            shutdown = true;
            instanceStorage.clear();
            serviceIndex.clear();
            healthyIndex.clear();
        }
    }

    @Override
    public ServiceInstance insert(ServiceInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("服务实例不能为空");
        }

        String instanceId = instance.instanceId();
        String serviceId = instance.serviceMeta().getServiceId();

        ServiceInstance existingInstance = instanceStorage.put(instanceId, instance);

        // 更新服务索引
        serviceIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);

        // 更新健康索引
        if (instance.getStatus().isAvailable()) {
            healthyIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);
            healthyInstances.incrementAndGet();
        }

        totalInstances.incrementAndGet();

        log.info("[DefaultInstanceManager] 实例添加: {} - {} - {}", 
            serviceId, instanceId, instance.getStatus());

        return existingInstance;
    }

    @Override
    public void deleteById(String instanceId) {
        if (instanceId == null) {
            return;
        }

        ServiceInstance removedInstance = instanceStorage.remove(instanceId);
        if (removedInstance != null) {
            String serviceId = removedInstance.serviceMeta().getServiceId();

            // 更新服务索引
            Set<String> serviceInstances = serviceIndex.get(serviceId);
            if (serviceInstances != null) {
                serviceInstances.remove(instanceId);
                if (serviceInstances.isEmpty()) {
                    serviceIndex.remove(serviceId);
                }
            }

            // 更新健康索引
            Set<String> healthyServiceInstances = healthyIndex.get(serviceId);
            if (healthyServiceInstances != null) {
                if (healthyServiceInstances.remove(instanceId)) {
                    healthyInstances.decrementAndGet();
                }
                if (healthyServiceInstances.isEmpty()) {
                    healthyIndex.remove(serviceId);
                }
            }

            totalInstances.decrementAndGet();
            log.info("[DefaultInstanceManager] 实例删除: {} - {}", serviceId, instanceId);
        }
    }

    @Override
    public ServiceInstance selectById(String instanceId) {
        return instanceStorage.get(instanceId);
    }

    @Override
    public Collection<ServiceInstance> selectAll() {
        return new ArrayList<>(instanceStorage.values());
    }

    @Override
    public List<ServiceInstance> getByServiceId(String serviceId) {
        if (serviceId == null) {
            return Collections.emptyList();
        }

        Set<String> instanceIds = serviceIndex.get(serviceId);
        if (instanceIds == null || instanceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return instanceIds.stream()
            .map(instanceStorage::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public ServiceInstance getByInstance(String serviceId, String instanceId) {
        if (serviceId == null || instanceId == null) {
            return null;
        }

        ServiceInstance instance = instanceStorage.get(instanceId);
        if (instance != null && serviceId.equals(instance.serviceMeta().getServiceId())) {
            return instance;
        }
        return null;
    }

    @Override
    public List<ServiceInstance> getHealthyInstances(String serviceId) {
        if (serviceId == null) {
            return Collections.emptyList();
        }

        Set<String> healthyInstanceIds = healthyIndex.get(serviceId);
        if (healthyInstanceIds == null || healthyInstanceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return healthyInstanceIds.stream()
            .map(instanceStorage::get)
            .filter(Objects::nonNull)
            .filter(instance -> instance.getStatus().isAvailable())
            .collect(Collectors.toList());
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceId, NodeStatus status) {
        if (serviceName == null || instanceId == null || status == null) {
            return;
        }

        ServiceInstance instance = instanceStorage.get(instanceId);
        if (instance == null || !serviceName.equals(instance.serviceMeta().getServiceId())) {
            return;
        }

        NodeStatus oldStatus = instance.getStatus();
        if (oldStatus == status) {
            return; // 状态未变化
        }

        // 更新健康索引
        updateHealthyIndex(serviceName, instanceId, oldStatus, status);

        log.info("[DefaultInstanceManager] 实例状态更新: {} - {} - {} -> {}", 
            serviceName, instanceId, oldStatus, status);
    }

    @Override
    public List<String> getAllServiceIds() {
        return new ArrayList<>(serviceIndex.keySet());
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInstances", totalInstances.get());
        stats.put("healthyInstances", healthyInstances.get());
        stats.put("totalServices", serviceIndex.size());
        
        long total = totalInstances.get();
        if (total > 0) {
            stats.put("healthyRate", (double) healthyInstances.get() / total);
        } else {
            stats.put("healthyRate", 0.0);
        }
        
        return stats;
    }

    /**
     * 更新健康索引
     */
    private void updateHealthyIndex(String serviceId, String instanceId, NodeStatus oldStatus, NodeStatus newStatus) {
        Set<String> healthyServiceInstances = healthyIndex.get(serviceId);
        
        boolean wasHealthy = oldStatus.isAvailable();
        boolean isHealthy = newStatus.isAvailable();
        
        if (wasHealthy && !isHealthy) {
            // 从健康变为不健康
            if (healthyServiceInstances != null) {
                healthyServiceInstances.remove(instanceId);
                healthyInstances.decrementAndGet();
                if (healthyServiceInstances.isEmpty()) {
                    healthyIndex.remove(serviceId);
                }
            }
        } else if (!wasHealthy && isHealthy) {
            // 从不健康变为健康
            healthyIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);
            healthyInstances.incrementAndGet();
        }
    }
} 