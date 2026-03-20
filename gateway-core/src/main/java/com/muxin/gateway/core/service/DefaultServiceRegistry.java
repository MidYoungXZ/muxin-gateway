package com.muxin.gateway.core.service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 默认服务注册中心实现
 * 统一管理静态配置和服务发现两种来源的实例
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultServiceRegistry implements ServiceRegistry {

    private final Map<String, ServiceInstance> instanceStorage = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> serviceIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> healthyIndex = new ConcurrentHashMap<>();
    private final Map<String, List<InstanceChangeListener>> listeners = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    @Override
    public void init() {
        if (!initialized) {
            log.info("[DefaultServiceRegistry] 服务注册中心初始化");
            initialized = true;
        }
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        if (!initialized) {
            throw new IllegalStateException("服务注册中心未初始化");
        }
        log.info("[DefaultServiceRegistry] 服务注册中心启动");
        started = true;
    }

    @Override
    public void shutdown() {
        if (!shutdown) {
            log.info("[DefaultServiceRegistry] 服务注册中心关闭");
            shutdown = true;
            instanceStorage.clear();
            serviceIndex.clear();
            healthyIndex.clear();
            listeners.clear();
        }
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
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
                .filter(ServiceInstance::isHealthy)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceInstance getInstance(String serviceId, String instanceId) {
        if (serviceId == null || instanceId == null) {
            return null;
        }
        ServiceInstance instance = instanceStorage.get(instanceId);
        if (instance != null && serviceId.equals(instance.getServiceId())) {
            return instance;
        }
        return null;
    }

    @Override
    public List<String> getAllServiceIds() {
        return new ArrayList<>(serviceIndex.keySet());
    }

    @Override
    public void registerInstance(ServiceInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("服务实例不能为空");
        }

        String instanceId = instance.getInstanceId();
        String serviceId = instance.getServiceId();

        ServiceInstance existing = instanceStorage.put(instanceId, instance);

        serviceIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);

        if (instance.isHealthy()) {
            healthyIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);
        }

        log.info("[DefaultServiceRegistry] 注册实例: {} - {} - {}",
                serviceId, instanceId, instance.getSource());

        notifyInstanceAdded(serviceId, instance);
    }

    @Override
    public void deregisterInstance(String serviceId, String instanceId) {
        if (serviceId == null || instanceId == null) {
            return;
        }

        ServiceInstance removed = instanceStorage.remove(instanceId);
        if (removed != null) {
            Set<String> serviceInstances = serviceIndex.get(serviceId);
            if (serviceInstances != null) {
                serviceInstances.remove(instanceId);
                if (serviceInstances.isEmpty()) {
                    serviceIndex.remove(serviceId);
                }
            }

            Set<String> healthyInstances = healthyIndex.get(serviceId);
            if (healthyInstances != null) {
                healthyInstances.remove(instanceId);
                if (healthyInstances.isEmpty()) {
                    healthyIndex.remove(serviceId);
                }
            }

            log.info("[DefaultServiceRegistry] 注销实例: {} - {}", serviceId, instanceId);
            notifyInstanceRemoved(serviceId, instanceId);
        }
    }

    @Override
    public void updateStatus(String serviceId, String instanceId, NodeStatus status) {
        if (serviceId == null || instanceId == null || status == null) {
            return;
        }

        ServiceInstance instance = instanceStorage.get(instanceId);
        if (instance == null || !serviceId.equals(instance.getServiceId())) {
            return;
        }

        if (instance instanceof DefaultServiceInstance) {
            DefaultServiceInstance defaultInstance = (DefaultServiceInstance) instance;
            boolean wasHealthy = defaultInstance.isHealthy();
            boolean isHealthy = status.isHealthy();

            defaultInstance.setHealthy(isHealthy);

            if (wasHealthy != isHealthy) {
                updateHealthyIndex(serviceId, instanceId, wasHealthy, isHealthy);
            }

            log.info("[DefaultServiceRegistry] 状态更新: {} - {} -> {}", instanceId, wasHealthy, isHealthy);
        }
    }

    @Override
    public void subscribe(String serviceId, InstanceChangeListener listener) {
        listeners.computeIfAbsent(serviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
        log.debug("[DefaultServiceRegistry] 订阅服务变更: {}", serviceId);
    }

    @Override
    public void unsubscribe(String serviceId, InstanceChangeListener listener) {
        List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
        if (serviceListeners != null) {
            serviceListeners.remove(listener);
            if (serviceListeners.isEmpty()) {
                listeners.remove(serviceId);
            }
        }
        log.debug("[DefaultServiceRegistry] 取消订阅服务变更: {}", serviceId);
    }

    @Override
    public boolean isRunning() {
        return started && !shutdown;
    }

    private void updateHealthyIndex(String serviceId, String instanceId, boolean wasHealthy, boolean isHealthy) {
        Set<String> healthyInstances = healthyIndex.get(serviceId);
        if (wasHealthy && !isHealthy) {
            if (healthyInstances != null) {
                healthyInstances.remove(instanceId);
                if (healthyInstances.isEmpty()) {
                    healthyIndex.remove(serviceId);
                }
            }
        } else if (!wasHealthy && isHealthy) {
            healthyIndex.computeIfAbsent(serviceId, k -> ConcurrentHashMap.newKeySet()).add(instanceId);
        }
    }

    private void notifyInstanceAdded(String serviceId, ServiceInstance instance) {
        List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
        if (serviceListeners != null) {
            for (InstanceChangeListener listener : serviceListeners) {
                try {
                    listener.onInstanceAdded(serviceId, instance);
                } catch (Exception e) {
                    log.error("[DefaultServiceRegistry] 通知监听器失败: {}", e.getMessage());
                }
            }
        }
    }

    private void notifyInstanceRemoved(String serviceId, String instanceId) {
        List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
        if (serviceListeners != null) {
            for (InstanceChangeListener listener : serviceListeners) {
                try {
                    listener.onInstanceRemoved(serviceId, instanceId);
                } catch (Exception e) {
                    log.error("[DefaultServiceRegistry] 通知监听器失败: {}", e.getMessage());
                }
            }
        }
    }
}