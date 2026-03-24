package com.muxin.gateway.cloud.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.muxin.gateway.core.service.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private final NacosRegistryProperties properties;
    private NamingService namingService;

    private final Map<String, List<InstanceChangeListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, EventListener> nacosListeners = new ConcurrentHashMap<>();
    private final Map<String, ServiceInstance> registeredInstances = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    public NacosServiceRegistry(NacosRegistryProperties properties) {
        this.properties = properties;
    }

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        try {
            log.info("[NacosServiceRegistry] 初始化 Nacos 服务注册中心: {}", properties.getServerAddr());

            Properties nacosProps = new Properties();
            nacosProps.setProperty("serverAddr", properties.getServerAddr());

            if (properties.getNamespace() != null && !properties.getNamespace().isEmpty()) {
                nacosProps.setProperty("namespace", properties.getNamespace());
            }

            if (properties.getUsername() != null && !properties.getUsername().isEmpty()) {
                nacosProps.setProperty("username", properties.getUsername());
            }
            if (properties.getPassword() != null && !properties.getPassword().isEmpty()) {
                nacosProps.setProperty("password", properties.getPassword());
            }

            if (properties.getAccessKey() != null && !properties.getAccessKey().isEmpty()) {
                nacosProps.setProperty("accessKey", properties.getAccessKey());
            }
            if (properties.getSecretKey() != null && !properties.getSecretKey().isEmpty()) {
                nacosProps.setProperty("secretKey", properties.getSecretKey());
            }

            if (properties.getContextPath() != null && !properties.getContextPath().isEmpty()) {
                nacosProps.setProperty("contextPath", properties.getContextPath());
            }

            if (properties.getClusterName() != null && !properties.getClusterName().isEmpty()) {
                nacosProps.setProperty("clusterName", properties.getClusterName());
            }

            nacosProps.setProperty("namingLoadCacheAtStart", properties.getNamingLoadCacheAtStart());

            if (properties.getLogName() != null && !properties.getLogName().isEmpty()) {
                nacosProps.setProperty("logName", properties.getLogName());
            }

            this.namingService = NamingFactory.createNamingService(nacosProps);

            initialized = true;
            log.info("[NacosServiceRegistry] Nacos 服务注册中心初始化完成");

        } catch (NacosException e) {
            log.error("[NacosServiceRegistry] 初始化 Nacos 失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化 Nacos 服务注册中心失败", e);
        }
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        if (!initialized) {
            init();
        }

        log.info("[NacosServiceRegistry] 启动 Nacos 服务注册中心");
        started = true;

        if (properties.isRegisterEnabled()) {
            registerGatewayInstance();
        }
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }

        log.info("[NacosServiceRegistry] 关闭 Nacos 服务注册中心");

        shutdown = true;

        for (Map.Entry<String, ServiceInstance> entry : registeredInstances.entrySet()) {
            try {
                ServiceInstance instance = entry.getValue();
                namingService.deregisterInstance(
                        instance.getServiceId(),
                        properties.getGroup(),
                        instance.getHost(),
                        instance.getPort()
                );
                log.info("[NacosServiceRegistry] 注销服务实例: {} - {}", instance.getServiceId(), instance.getInstanceId());
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistry] 注销服务实例失败: {}", e.getMessage());
            }
        }
        registeredInstances.clear();

        for (Map.Entry<String, EventListener> entry : nacosListeners.entrySet()) {
            try {
                namingService.unsubscribe(entry.getKey(), properties.getGroup(), entry.getValue());
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistry] 取消订阅失败: {}", e.getMessage());
            }
        }
        nacosListeners.clear();
        listeners.clear();

        if (namingService != null) {
            try {
                namingService.shutDown();
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistry] 关闭 NamingService 失败: {}", e.getMessage());
            }
        }

        log.info("[NacosServiceRegistry] Nacos 服务注册中心已关闭");
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        checkState();
        try {
            List<Instance> instances = namingService.getAllInstances(serviceId, properties.getGroup());
            return instances.stream()
                    .map(inst -> new NacosServiceInstance(serviceId, inst))
                    .collect(Collectors.toList());
        } catch (NacosException e) {
            if (log.isDebugEnabled()) {
                log.debug("[NacosServiceRegistry] 获取服务实例失败: {} - {}", serviceId, e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    @Override
    public List<ServiceInstance> getHealthyInstances(String serviceId) {
        checkState();
        try {
            List<Instance> instances = namingService.selectInstances(
                    serviceId,
                    properties.getGroup(),
                    true
            );
            return instances.stream()
                    .map(inst -> new NacosServiceInstance(serviceId, inst))
                    .collect(Collectors.toList());
        } catch (NacosException e) {
            if (log.isDebugEnabled()) {
                log.debug("[NacosServiceRegistry] 获取健康实例失败: {} - {}", serviceId, e.getMessage());
            }
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
        try {
            ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, properties.getGroup());
            return services.getData();
        } catch (NacosException e) {
            log.warn("[NacosServiceRegistry] 获取服务列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void registerInstance(ServiceInstance instance) {
        checkState();
        if (instance == null) {
            throw new IllegalArgumentException("服务实例不能为空");
        }

        try {
            Instance nacosInstance = convertToNacosInstance(instance);
            namingService.registerInstance(
                    instance.getServiceId(),
                    properties.getGroup(),
                    nacosInstance
            );

            registeredInstances.put(instance.getInstanceId(), instance);
            log.info("[NacosServiceRegistry] 注册服务实例: {} - {}:{}",
                    instance.getServiceId(), instance.getHost(), instance.getPort());

        } catch (NacosException e) {
            log.error("[NacosServiceRegistry] 注册服务实例失败: {}", e.getMessage(), e);
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
                namingService.deregisterInstance(
                        serviceId,
                        properties.getGroup(),
                        instance.getHost(),
                        instance.getPort()
                );
                log.info("[NacosServiceRegistry] 注销服务实例: {} - {}", serviceId, instanceId);
            } catch (NacosException e) {
                log.warn("[NacosServiceRegistry] 注销服务实例失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public void updateStatus(String serviceId, String instanceId, NodeStatus status) {
        log.info("[NacosServiceRegistry] 更新实例状态: {} - {} -> {}", serviceId, instanceId, status);
    }

    @Override
    public void subscribe(String serviceId, InstanceChangeListener listener) {
        checkState();

        listeners.computeIfAbsent(serviceId, k -> new CopyOnWriteArrayList<>()).add(listener);

        if (!nacosListeners.containsKey(serviceId)) {
            EventListener nacosListener = event -> handleNacosEvent(serviceId, event);
            try {
                namingService.subscribe(serviceId, properties.getGroup(), nacosListener);
                nacosListeners.put(serviceId, nacosListener);
                log.info("[NacosServiceRegistry] 订阅服务变更: {}", serviceId);
            } catch (NacosException e) {
                log.error("[NacosServiceRegistry] 订阅服务失败: {}", e.getMessage(), e);
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

                EventListener nacosListener = nacosListeners.remove(serviceId);
                if (nacosListener != null) {
                    try {
                        namingService.unsubscribe(serviceId, properties.getGroup(), nacosListener);
                        log.info("[NacosServiceRegistry] 取消订阅服务: {}", serviceId);
                    } catch (NacosException e) {
                        log.warn("[NacosServiceRegistry] 取消订阅失败: {}", e.getMessage());
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
        if (!initialized || !started) {
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
        nacosInstance.setEphemeral(properties.isEphemeral());

        if (properties.getCluster() != null && !properties.getCluster().isEmpty()) {
            nacosInstance.setClusterName(properties.getCluster());
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.putAll(instance.getMetadata());
        metadata.putAll(properties.getServiceMetadata());
        metadata.put("scheme", instance.getScheme());
        nacosInstance.setMetadata(metadata);

        return nacosInstance;
    }

    private void handleNacosEvent(String serviceId, Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent namingEvent = (NamingEvent) event;
            List<Instance> instances = namingEvent.getInstances();

            if (log.isDebugEnabled()) {
                log.debug("[NacosServiceRegistry] 服务变更: {} - 实例数量: {}", serviceId, instances.size());
            }

            List<InstanceChangeListener> serviceListeners = listeners.get(serviceId);
            if (serviceListeners != null) {
                for (InstanceChangeListener listener : serviceListeners) {
                    try {
                        for (Instance inst : instances) {
                            ServiceInstance serviceInstance = new NacosServiceInstance(serviceId, inst);
                            listener.onInstanceAdded(serviceId, serviceInstance);
                        }
                    } catch (Exception e) {
                        log.error("[NacosServiceRegistry] 通知监听器失败: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private void registerGatewayInstance() {
        String serviceName = properties.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            log.info("[NacosServiceRegistry] 未配置服务名，跳过网关注册");
            return;
        }

        String ip = properties.getIp();
        if (ip == null || ip.isEmpty()) {
            ip = getLocalIp();
        }

        ServiceInstance gatewayInstance = DefaultServiceInstance.builder()
                .serviceId(serviceName)
                .instanceId(serviceName + "-" + ip + "-" + properties.getPort())
                .host(ip)
                .port(properties.getPort())
                .scheme("http")
                .weight(properties.getWeight())
                .healthy(true)
                .source(InstanceSource.DISCOVERY)
                .metadata(properties.getServiceMetadata())
                .build();

        registerInstance(gatewayInstance);
    }

    private String getLocalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}