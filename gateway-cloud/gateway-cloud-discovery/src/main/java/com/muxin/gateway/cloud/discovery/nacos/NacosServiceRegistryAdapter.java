package com.muxin.gateway.cloud.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.muxin.gateway.core.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class NacosServiceRegistryAdapter implements ServiceRegistry {

    private final DiscoveryClient discoveryClient;
    private final NamingService namingService;
    private final String group;

    private final Map<String, List<InstanceChangeListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, EventListener> nacosListeners = new ConcurrentHashMap<>();
    private final Map<String, ServiceInstance> registeredInstances = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ServiceInstance>> knownInstances = new ConcurrentHashMap<>();

    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    public NacosServiceRegistryAdapter(DiscoveryClient discoveryClient, NamingService namingService, String group) {
        this.discoveryClient = discoveryClient;
        this.namingService = namingService;
        this.group = group != null ? group : "DEFAULT_GROUP";
    }

    @Override
    public void init() {
        log.info("[NacosServiceRegistryAdapter] 初始化完成");
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        log.info("[NacosServiceRegistryAdapter] 启动 Nacos 服务注册中心");
        started = true;
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }

        log.info("[NacosServiceRegistryAdapter] 关闭 Nacos 服务注册中心");
        shutdown = true;

        for (Map.Entry<String, ServiceInstance> entry : registeredInstances.entrySet()) {
            try {
                ServiceInstance instance = entry.getValue();
                namingService.deregisterInstance(
                        instance.getServiceId(),
                        group,
                        instance.getHost(),
                        instance.getPort()
                );
                log.info("[NacosServiceRegistryAdapter] 注销服务实例: {} - {}", instance.getServiceId(), instance.getInstanceId());
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistryAdapter] 注销服务实例失败: {}", e.getMessage());
            }
        }
        registeredInstances.clear();

        for (Map.Entry<String, EventListener> entry : nacosListeners.entrySet()) {
            try {
                namingService.unsubscribe(entry.getKey(), group, entry.getValue());
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistryAdapter] 取消订阅失败: {}", e.getMessage());
            }
        }
        nacosListeners.clear();
        listeners.clear();
        knownInstances.clear();

        log.info("[NacosServiceRegistryAdapter] Nacos 服务注册中心已关闭");
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        checkState();
        try {
            List<Instance> instances = namingService.getAllInstances(serviceId, group);
            return instances.stream()
                    .map(inst -> new NacosServiceInstance(serviceId, inst))
                    .collect(Collectors.toList());
        } catch (NacosException e) {
            log.warn("[NacosServiceRegistryAdapter] 获取服务实例失败: {} - {}", serviceId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ServiceInstance> getHealthyInstances(String serviceId) {
        checkState();
        try {
            List<Instance> instances = namingService.selectInstances(serviceId, group, true);
            return instances.stream()
                    .map(inst -> new NacosServiceInstance(serviceId, inst))
                    .collect(Collectors.toList());
        } catch (NacosException e) {
            log.warn("[NacosServiceRegistryAdapter] 获取健康实例失败: {} - {}", serviceId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ServiceInstance getInstance(String serviceId, String instanceId) {
        List<ServiceInstance> instances = getInstances(serviceId);
        return instances.stream()
                .filter(inst -> instanceId.equals(inst.getInstanceId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAllServiceIds() {
        checkState();
        return discoveryClient.getServices();
    }

    @Override
    public void registerInstance(ServiceInstance instance) {
        checkState();
        if (instance == null) {
            throw new IllegalArgumentException("服务实例不能为空");
        }

        try {
            Instance nacosInstance = convertToNacosInstance(instance);
            namingService.registerInstance(instance.getServiceId(), group, nacosInstance);

            registeredInstances.put(instance.getInstanceId(), instance);
            log.info("[NacosServiceRegistryAdapter] 注册服务实例: {} - {}:{}",
                    instance.getServiceId(), instance.getHost(), instance.getPort());

        } catch (NacosException e) {
            log.error("[NacosServiceRegistryAdapter] 注册服务实例失败: {}", e.getMessage(), e);
            throw new RuntimeException("注册服务实例失败", e);
        }
    }

    @Override
    public void deregisterInstance(String serviceId, String instanceId) {
        checkState();
        ServiceInstance instance = registeredInstances.remove(instanceId);
        if (instance == null) {
            instance = getInstance(serviceId, instanceId);
        }

        if (instance != null) {
            try {
                namingService.deregisterInstance(serviceId, group, instance.getHost(), instance.getPort());
                log.info("[NacosServiceRegistryAdapter] 注销服务实例: {} - {}", serviceId, instanceId);
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistryAdapter] 注销服务实例失败: {}", e.getMessage());
                throw new RuntimeException("注销服务实例失败: " + serviceId + " - " + instanceId, e);
            }
        }
    }

    @Override
    public void updateStatus(String serviceId, String instanceId, NodeStatus status) {
        checkState();
        if (status == null) {
            throw new IllegalArgumentException("节点状态不能为空");
        }
        ServiceInstance instance = registeredInstances.get(instanceId);
        if (instance == null) {
            instance = getInstance(serviceId, instanceId);
        }
        if (instance == null) {
            log.warn("[NacosServiceRegistryAdapter] 未找到实例，跳过状态更新: {} - {}", serviceId, instanceId);
            return;
        }

        try {
            Instance nacosInstance = convertToNacosInstance(instance);
            nacosInstance.setHealthy(status.isHealthy());
            nacosInstance.setEnabled(status.isAvailable());
            namingService.deregisterInstance(serviceId, group, instance.getHost(), instance.getPort());
            namingService.registerInstance(serviceId, group, nacosInstance);
            if (instance instanceof DefaultServiceInstance defaultInstance) {
                defaultInstance.setHealthy(status.isHealthy());
            }
            log.info("[NacosServiceRegistryAdapter] 更新实例状态: {} - {} -> {}", serviceId, instanceId, status);
        } catch (NacosException e) {
            throw new RuntimeException("更新实例状态失败: " + serviceId + " - " + instanceId, e);
        }
    }

    @Override
    public void subscribe(String serviceId, InstanceChangeListener listener) {
        checkState();

        listeners.computeIfAbsent(serviceId, k -> new CopyOnWriteArrayList<>()).add(listener);

        if (!nacosListeners.containsKey(serviceId)) {
            EventListener nacosListener = event -> handleNacosEvent(serviceId, event);
            try {
                namingService.subscribe(serviceId, group, nacosListener);
                nacosListeners.put(serviceId, nacosListener);
                knownInstances.put(serviceId, snapshotInstances(serviceId));
                log.info("[NacosServiceRegistryAdapter] 订阅服务变更: {}", serviceId);
            } catch (NacosException e) {
                log.error("[NacosServiceRegistryAdapter] 订阅服务失败: {}", e.getMessage(), e);
                throw new RuntimeException("订阅服务失败: " + serviceId, e);
            }
        }
    }

    @Override
    public void unsubscribe(String serviceId, InstanceChangeListener listener) {
        List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
        if (serviceListeners != null) {
            serviceListeners.remove(listener);
            if (serviceListeners.isEmpty()) {
                listeners.remove(serviceId);
                knownInstances.remove(serviceId);

                EventListener nacosListener = nacosListeners.remove(serviceId);
                if (nacosListener != null) {
                    try {
                        namingService.unsubscribe(serviceId, group, nacosListener);
                        log.info("[NacosServiceRegistryAdapter] 取消订阅服务: {}", serviceId);
                    } catch (NacosException e) {
                        log.warn("[NacosServiceRegistryAdapter] 取消订阅失败: {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        return started && !shutdown;
    }

    public NamingService getNamingService() {
        return namingService;
    }

    private void checkState() {
        if (!started) {
            throw new IllegalStateException("服务注册中心未启动");
        }
        if (shutdown) {
            throw new IllegalStateException("服务注册中心已关闭");
        }
    }

    private Instance convertToNacosInstance(ServiceInstance instance) {
        Instance nacosInstance = new Instance();
        nacosInstance.setIp(instance.getHost());
        nacosInstance.setPort(instance.getPort());
        nacosInstance.setWeight((float) instance.getWeight());
        nacosInstance.setHealthy(instance.isHealthy());
        nacosInstance.setEnabled(true);
        nacosInstance.setEphemeral(true);
        nacosInstance.setMetadata(instance.getMetadata());
        return nacosInstance;
    }

    private void handleNacosEvent(String serviceId, Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent namingEvent = (NamingEvent) event;
            List<Instance> instances = namingEvent.getInstances();

            if (log.isDebugEnabled()) {
                log.debug("[NacosServiceRegistryAdapter] 服务变更: {} - 实例数量: {}", serviceId, instances.size());
            }

            Map<String, ServiceInstance> current = instances.stream()
                    .map(instance -> new NacosServiceInstance(serviceId, instance))
                    .collect(Collectors.toMap(ServiceInstance::getInstanceId, instance -> instance, (left, right) -> right));
            Map<String, ServiceInstance> previous = knownInstances.put(serviceId, current);
            if (previous == null) {
                previous = Collections.emptyMap();
            }

            List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
            if (serviceListeners == null) {
                return;
            }
            for (InstanceChangeListener listener : serviceListeners) {
                try {
                    for (ServiceInstance instance : current.values()) {
                        ServiceInstance oldInstance = previous.get(instance.getInstanceId());
                        if (oldInstance == null) {
                            listener.onInstanceAdded(serviceId, instance);
                        } else if (oldInstance.isHealthy() != instance.isHealthy()) {
                            listener.onInstanceStatusChanged(serviceId, instance.getInstanceId(), statusOf(oldInstance), statusOf(instance));
                        }
                    }
                    for (String instanceId : previous.keySet()) {
                        if (!current.containsKey(instanceId)) {
                            listener.onInstanceRemoved(serviceId, instanceId);
                        }
                    }
                } catch (Exception e) {
                    log.error("[NacosServiceRegistryAdapter] 通知监听器失败: {}", e.getMessage());
                }
            }
        }
    }

    private Map<String, ServiceInstance> snapshotInstances(String serviceId) {
        return getInstances(serviceId).stream()
                .collect(Collectors.toMap(ServiceInstance::getInstanceId, instance -> instance, (left, right) -> right));
    }

    private NodeStatus statusOf(ServiceInstance instance) {
        return instance.isHealthy() ? NodeStatus.HEALTHY : NodeStatus.UNAVAILABLE;
    }
}
