package com.muxin.gateway.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxin.gateway.registry.api.RegisterCenter;
import com.muxin.gateway.registry.api.RegisterCenterListener;
import com.muxin.gateway.registry.api.ServiceInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Nacos注册中心实现
 *
 * @author Administrator
 * @date 2025/6/13 18:10
 */
@Data
@Slf4j
public class NacosRegisterCenter implements RegisterCenter {

    private static final String METADATA_KEY = "metadata";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String registerAddress;
    private final String groupName;
    private final String clusterName;

    // 主要用于维护服务实例信息
    private NamingService namingService;
    // 主要用于维护服务定义信息
    private NamingMaintainService namingMaintainService;
    // 监听器列表
    private final Map<String, List<RegisterCenterListener>> listeners = new ConcurrentHashMap<>();
    // 已订阅的服务
    private final Map<String, NacosRegisterListener> subscribedServices = new ConcurrentHashMap<>();

    public NacosRegisterCenter(String registerAddress, String groupName, String clusterName) {
        this.registerAddress = registerAddress;
        this.groupName = groupName;
        this.clusterName = clusterName;
        init();
    }

    private void init() {
        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
            log.info("Nacos register center initialized successfully. Address: {}, Group: {}, Cluster: {}", 
                registerAddress, groupName, clusterName);
        } catch (NacosException e) {
            log.error("Failed to initialize Nacos register center", e);
            throw new RuntimeException("NacosRegisterCenter init failed", e);
        }
    }

    @Override
    public void register(ServiceInstance instance) {
        try {
            // 构造nacos实例信息
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(instance.getInstanceId());
            nacosInstance.setPort(instance.getPort());
            nacosInstance.setIp(instance.getHost());
            nacosInstance.setWeight(instance.getWeight());
            nacosInstance.setHealthy(instance.isHealthy());
            nacosInstance.setClusterName(clusterName);
            
            // 设置元数据
            Map<String, String> metadata = new ConcurrentHashMap<>(instance.getMetadata());
            metadata.put(METADATA_KEY, toJson(instance));
            nacosInstance.setMetadata(metadata);

            // 注册实例
            namingService.registerInstance(instance.getServiceDefinition().getServiceId(), 
                groupName, nacosInstance);

            log.info("Successfully registered service instance: {}", instance.getInstanceId());
        } catch (Exception e) {
            log.error("Failed to register service instance: {}", instance, e);
            throw new RuntimeException("Service registration failed", e);
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            namingService.deregisterInstance(instance.getServiceDefinition().getServiceId(),
                groupName, instance.getHost(), instance.getPort(), clusterName);
            
            log.info("Successfully deregistered service instance: {}", instance.getInstanceId());
        } catch (NacosException e) {
            log.error("Failed to deregister service instance: {}", instance, e);
            throw new RuntimeException("Service deregistration failed", e);
        }
    }

    @Override
    public List<ServiceInstance> selectInstances(String serviceId) {
        return selectInstances(serviceId, true);
    }

    @Override
    public List<ServiceInstance> selectInstances(String serviceId, Boolean healthy) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceId, groupName, healthy);
            List<ServiceInstance> result = new ArrayList<>();
            
            for (Instance instance : instances) {
                ServiceInstance serviceInstance = fromNacosInstance(instance);
                if (serviceInstance != null) {
                    result.add(serviceInstance);
                }
            }
            
            return result;
        } catch (NacosException e) {
            log.error("Failed to select instances for service: {}", serviceId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void subscribe(String serviceId, RegisterCenterListener listener) {
        listeners.computeIfAbsent(serviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
        
        // 如果还没有订阅这个服务，则订阅
        if (!subscribedServices.containsKey(serviceId)) {
            try {
                NacosRegisterListener nacosListener = new NacosRegisterListener(serviceId);
                namingService.subscribe(serviceId, groupName, nacosListener);
                subscribedServices.put(serviceId, nacosListener);
                
                log.info("Successfully subscribed to service: {}", serviceId);
            } catch (NacosException e) {
                log.error("Failed to subscribe to service: {}", serviceId, e);
                throw new RuntimeException("Service subscription failed", e);
            }
        }
    }

    @Override
    public void unsubscribe(String serviceId, RegisterCenterListener listener) {
        List<RegisterCenterListener> serviceListeners = listeners.get(serviceId);
        if (serviceListeners != null) {
            serviceListeners.remove(listener);
            
            // 如果没有监听器了，取消订阅
            if (serviceListeners.isEmpty()) {
                listeners.remove(serviceId);
                NacosRegisterListener nacosListener = subscribedServices.remove(serviceId);
                if (nacosListener != null) {
                    try {
                        namingService.unsubscribe(serviceId, groupName, nacosListener);
                        log.info("Successfully unsubscribed from service: {}", serviceId);
                    } catch (NacosException e) {
                        log.error("Failed to unsubscribe from service: {}", serviceId, e);
                    }
                }
            }
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            return namingService.getServerStatus().equals("UP");
        } catch (Exception e) {
            log.warn("Failed to check Nacos server status", e);
            return false;
        }
    }

    @Override
    public void shutdown() {
        try {
            if (namingService != null) {
                namingService.shutDown();
            }
            if (namingMaintainService != null) {
                namingMaintainService.shutDown();
            }
            
            listeners.clear();
            subscribedServices.clear();
            
            log.info("Nacos register center shutdown successfully");
        } catch (Exception e) {
            log.error("Failed to shutdown Nacos register center", e);
        }
    }

    private ServiceInstance fromNacosInstance(Instance instance) {
        try {
            String metadataJson = instance.getMetadata().get(METADATA_KEY);
            if (metadataJson != null) {
                return fromJson(metadataJson, DefaultServiceInstance.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse service instance from Nacos instance metadata", e);
        }
        return null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to object", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Nacos事件监听器
     */
    private class NacosRegisterListener implements EventListener {
        
        private final String serviceId;
        
        public NacosRegisterListener(String serviceId) {
            this.serviceId = serviceId;
        }

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent namingEvent) {
                try {
                    List<Instance> instances = namingEvent.getInstances();
                    List<ServiceInstance> serviceInstances = new ArrayList<>();

                    for (Instance instance : instances) {
                        ServiceInstance serviceInstance = fromNacosInstance(instance);
                        if (serviceInstance != null) {
                            serviceInstances.add(serviceInstance);
                        }
                    }

                    // 通知所有监听器
                    List<RegisterCenterListener> serviceListeners = listeners.get(serviceId);
                    if (serviceListeners != null) {
                        for (RegisterCenterListener listener : serviceListeners) {
                            try {
                                listener.onChange(serviceInstances);
                            } catch (Exception e) {
                                log.error("Error notifying listener for service: {}", serviceId, e);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing Nacos naming event for service: {}", serviceId, e);
                }
            }
        }
    }
} 