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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxin.gateway.core.registry.DefaultServiceInstance;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.RegisterCenterListener;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    private final String username;
    private final String password;

    // 主要用于维护服务实例信息
    private NamingService namingService;
    // 主要用于维护服务定义信息
    private NamingMaintainService namingMaintainService;
    // 监听器列表
    private final Map<String, List<RegisterCenterListener>> listeners = new ConcurrentHashMap<>();
    // 已订阅的服务
    private final Map<String, NacosRegisterListener> subscribedServices = new ConcurrentHashMap<>();

    public NacosRegisterCenter(String registerAddress, String groupName, String clusterName) {
        this(registerAddress, groupName, clusterName, null, null);
    }

    public NacosRegisterCenter(String registerAddress, String groupName, String clusterName, 
                              String username, String password) {
        this.registerAddress = registerAddress;
        this.groupName = groupName;
        this.clusterName = clusterName;
        this.username = username;
        this.password = password;
        init();
    }

    protected void init() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", registerAddress);
            
            // 如果配置了用户名密码，则添加认证信息
            if (username != null && !username.trim().isEmpty() && 
                password != null && !password.trim().isEmpty()) {
                properties.setProperty("username", username);
                properties.setProperty("password", password);
                log.info("Nacos authentication enabled for user: {}", username);
            }
            
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(properties);
            this.namingService = NamingFactory.createNamingService(properties);
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
            nacosInstance.setEnabled(instance.getServiceDefinition().isEnabled());
            nacosInstance.setClusterName(clusterName);
            nacosInstance.setServiceName(instance.getServiceDefinition().getServiceId());
            
            // 设置元数据，包含我们的完整ServiceInstance信息和通用字段
            Map<String, String> metadata = new ConcurrentHashMap<>();
            if (instance.getMetadata() != null) {
                metadata.putAll(instance.getMetadata());
            }
            
            // 添加通用字段到metadata
            metadata.put("scheme", instance.getServiceDefinition().getScheme());
            metadata.put("version", instance.getServiceDefinition().getVersion());
            metadata.put("secure", String.valueOf(instance.isSecure()));
            if (instance.getServiceDefinition().getDescription() != null && 
                !instance.getServiceDefinition().getDescription().isEmpty()) {
                metadata.put("description", instance.getServiceDefinition().getDescription());
            }
            
            // 保存完整的ServiceInstance信息（用于我们自己的应用）
            metadata.put(METADATA_KEY, toJson(instance));
            
            nacosInstance.setMetadata(metadata);

            // 注册实例
            namingService.registerInstance(instance.getServiceDefinition().getServiceId(), 
                groupName, nacosInstance);

            log.info("Successfully registered service instance: {} for service: {}", 
                instance.getInstanceId(), instance.getServiceDefinition().getServiceId());
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
            // 优先尝试从我们自己的metadata中反序列化完整的ServiceInstance
            String metadataJson = instance.getMetadata().get(METADATA_KEY);
            if (metadataJson != null && !metadataJson.isEmpty()) {
                try {
                    return fromJson(metadataJson, DefaultServiceInstance.class);
                } catch (Exception e) {
                    log.debug("Failed to parse ServiceInstance from metadata, falling back to field mapping", e);
                }
            }
            
            // 通用转换逻辑：直接从Nacos Instance字段映射
            return createServiceInstanceFromNacosInstance(instance);
            
        } catch (Exception e) {
            log.warn("Failed to convert Nacos instance to ServiceInstance: {}", instance, e);
            return null;
        }
    }
    
    /**
     * 通用的Nacos Instance到ServiceInstance转换方法
     * 直接从Nacos Instance的字段映射，不依赖特定的metadata格式
     */
    private ServiceInstance createServiceInstanceFromNacosInstance(Instance instance) {
        // 创建服务定义
        DefaultServiceInstance.DefaultServiceDefinition serviceDefinition = 
            new DefaultServiceInstance.DefaultServiceDefinition();
        serviceDefinition.setServiceId(instance.getServiceName());
        serviceDefinition.setScheme(extractSchemeFromMetadata(instance.getMetadata()));
        serviceDefinition.setVersion(extractVersionFromMetadata(instance.getMetadata()));
        serviceDefinition.setScope("public");
        serviceDefinition.setEnabled(instance.isEnabled());
        serviceDefinition.setDescription(extractDescriptionFromMetadata(instance.getMetadata()));
        
        // 创建服务实例
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance();
        serviceInstance.setServiceDefinition(serviceDefinition);
        serviceInstance.setInstanceId(instance.getInstanceId() != null ? 
            instance.getInstanceId() : generateInstanceId(instance));
        serviceInstance.setHost(instance.getIp());
        serviceInstance.setPort(instance.getPort());
        serviceInstance.setWeight(instance.getWeight());
        serviceInstance.setHealthy(instance.isHealthy());
        serviceInstance.setSecure(isSecureFromMetadata(instance.getMetadata()));
        
        // 构建URI
        String scheme = serviceDefinition.getScheme();
        serviceInstance.setUri(java.net.URI.create(
            scheme + "://" + instance.getIp() + ":" + instance.getPort()));
        
        // 复制metadata（过滤掉我们内部使用的key）
        Map<String, String> metadata = new java.util.HashMap<>();
        if (instance.getMetadata() != null) {
            for (Map.Entry<String, String> entry : instance.getMetadata().entrySet()) {
                // 跳过我们内部使用的metadata key
                if (!METADATA_KEY.equals(entry.getKey())) {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }
        serviceInstance.setMetadata(metadata);
        
        return serviceInstance;
    }
    
    /**
     * 从metadata中提取协议类型，默认为http
     */
    private String extractSchemeFromMetadata(Map<String, String> metadata) {
        if (metadata == null) return "http";
        
        String scheme = metadata.get("scheme");
        if (scheme != null) return scheme;
        
        String secure = metadata.get("secure");
        if ("true".equalsIgnoreCase(secure)) return "https";
        
        return "http";
    }
    
    /**
     * 从metadata中提取版本信息
     */
    private String extractVersionFromMetadata(Map<String, String> metadata) {
        if (metadata == null) return "1.0";
        return metadata.getOrDefault("version", "1.0");
    }
    
    /**
     * 从metadata中提取描述信息
     */
    private String extractDescriptionFromMetadata(Map<String, String> metadata) {
        if (metadata == null) return "";
        return metadata.getOrDefault("description", "");
    }
    
    /**
     * 从metadata中判断是否为安全连接
     */
    private boolean isSecureFromMetadata(Map<String, String> metadata) {
        if (metadata == null) return false;
        
        String secure = metadata.get("secure");
        if (secure != null) return Boolean.parseBoolean(secure);
        
        String scheme = metadata.get("scheme");
        return "https".equalsIgnoreCase(scheme);
    }
    
    /**
     * 生成实例ID（当Nacos实例没有instanceId时）
     */
    private String generateInstanceId(Instance instance) {
        return instance.getServiceName() + "-" + 
               instance.getIp() + "-" + 
               instance.getPort() + "-" + 
               System.currentTimeMillis();
    }

    /**
     * 测试方法：验证Nacos Instance到ServiceInstance的转换
     * 包级别可见，供测试使用
     */
    ServiceInstance testConvertFromNacosInstance(Instance nacosInstance) {
        return fromNacosInstance(nacosInstance);
    }

    /**
     * 测试方法：验证通用转换逻辑
     * 包级别可见，供测试使用
     */
    ServiceInstance testCreateServiceInstanceFromNacosInstance(Instance nacosInstance) {
        return createServiceInstanceFromNacosInstance(nacosInstance);
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
            if (event instanceof NamingEvent) {
                try {
                    NamingEvent namingEvent = (NamingEvent) event;
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