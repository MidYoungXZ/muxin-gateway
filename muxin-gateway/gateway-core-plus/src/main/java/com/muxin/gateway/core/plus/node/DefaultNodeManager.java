package com.muxin.gateway.core.plus.node;

import com.muxin.gateway.core.plus.ServiceDiscovery;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.ProtocolType;
import com.muxin.gateway.core.plus.monitor.MetricsRegistry;
import com.muxin.gateway.core.plus.monitor.MonitorMetadata;
import com.muxin.gateway.core.plus.monitor.MonitorType;
import com.muxin.gateway.core.plus.node.health.HealthCheckResult;
import com.muxin.gateway.core.plus.node.health.HealthChecker;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 默认节点管理器实现
 * 
 * @author muxin
 */
@Slf4j
public class DefaultNodeManager implements NodeManager {
    
    // 直接按节点ID管理 - Repository 接口需要
    private final Map<String, ServiceNode> nodes;
    // 按服务名分组管理 - 业务方法需要  
    private final Map<String, Map<String, ServiceNode>> serviceNodes;
    private final ServiceDiscovery serviceDiscovery;
    private final HealthChecker healthChecker;
    private final ScheduledExecutorService scheduler;
    private final List<ServiceChangeListener> listeners;
    private volatile boolean running = false;
    
    public DefaultNodeManager() {
        this.nodes = new ConcurrentHashMap<>();
        this.serviceNodes = new ConcurrentHashMap<>();
        this.serviceDiscovery = new DefaultServiceDiscovery();
        this.healthChecker = new DefaultHealthChecker();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listeners = new ArrayList<>();
    }
    
    // Repository 接口实现
    @Override
    public ServiceNode save(ServiceNode entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Node and node ID cannot be null");
        }
        
        // 从节点的元数据中获取服务名称，如果没有则使用默认值
        String serviceName = getServiceNameFromNode(entity);
        
        // 同时更新两个数据结构
        nodes.put(entity.getId(), entity);
        serviceNodes.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                   .put(entity.getId(), entity);
        
        log.info("保存节点: {}/{} - {}", serviceName, entity.getId(), entity.getAddress().toUri());
        
        // 通知监听器
        notifyListeners(listener -> listener.onNodeAdded(serviceName, entity));
        
        return entity;
    }
    
    @Override
    public void removeByUniqueCode(String nodeId) {
        if (nodeId == null) {
            return;
        }
        
        ServiceNode removed = nodes.remove(nodeId);
        if (removed != null) {
            String serviceName = getServiceNameFromNode(removed);
            
            // 从服务映射中移除
            Map<String, ServiceNode> serviceNodeMap = serviceNodes.get(serviceName);
            if (serviceNodeMap != null) {
                serviceNodeMap.remove(nodeId);
                // 如果服务没有节点了，移除服务
                if (serviceNodeMap.isEmpty()) {
                    serviceNodes.remove(serviceName);
                }
            }
            
            log.info("移除节点: {}/{}", serviceName, nodeId);
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeRemoved(serviceName, nodeId));
        }
    }
    
    @Override
    public ServiceNode findByUniqueCode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    @Override
    public Collection<ServiceNode> findAll() {
        return new ArrayList<>(nodes.values());
    }
    
    // 业务特定方法
    @Override
    public List<ServiceNode> getNodes(String serviceName) {
        Map<String, ServiceNode> serviceNodeMap = serviceNodes.get(serviceName);
        return serviceNodeMap != null ? new ArrayList<>(serviceNodeMap.values()) : new ArrayList<>();
    }
    
    @Override
    public ServiceNode getNode(String serviceName, String nodeId) {
        Map<String, ServiceNode> serviceNodeMap = serviceNodes.get(serviceName);
        return serviceNodeMap != null ? serviceNodeMap.get(nodeId) : null;
    }
    
    @Override
    public List<ServiceNode> getHealthyNodes(String serviceName) {
        return getNodes(serviceName).stream()
                .filter(ServiceNode::isHealthy)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateNodeStatus(String serviceName, String nodeId, NodeStatus status) {
        ServiceNode node = nodes.get(nodeId);
        if (node != null) {
            NodeStatus oldStatus = node.getStatus();
            node.updateStatus(status);
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeStatusChanged(serviceName, nodeId, oldStatus, status));
        }
    }
    
    @Override
    public List<String> getAllServiceNames() {
        return new ArrayList<>(serviceNodes.keySet());
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        // 启动健康检查
        startHealthCheck();
        
        // 启动服务发现
        serviceDiscovery.start();
        
        log.info("节点管理器启动完成");
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        // 停止健康检查
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 停止服务发现
        serviceDiscovery.shutdown();
        
        log.info("节点管理器停止完成");
    }
    
    /**
     * 从节点中获取服务名称
     */
    private String getServiceNameFromNode(ServiceNode node) {
        if (node.getMetadata() != null && node.getMetadata().containsKey("serviceName")) {
            return (String) node.getMetadata().get("serviceName");
        }
        // 默认使用节点名称作为服务名称
        return node.getName() != null ? node.getName() : "default-service";
    }
    
    /**
     * 获取所有节点（用于兼容）
     */
    public List<ServiceNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }
    
    /**
     * 添加服务变化监听器
     */
    public void addServiceChangeListener(ServiceChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除服务变化监听器
     */
    public void removeServiceChangeListener(ServiceChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * 启动健康检查
     */
    private void startHealthCheck() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                performHealthCheck();
            } catch (Exception e) {
                log.error("健康检查失败: {}", e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        for (ServiceNode node : nodes.values()) {
            try {
                HealthCheckResult result = healthChecker.checkHealth(node);
                
                NodeStatus oldStatus = node.getStatus();
                NodeStatus newStatus = result.isHealthy() ? NodeStatus.HEALTHY : NodeStatus.UNHEALTHY;
                
                if (oldStatus != newStatus) {
                    node.updateStatus(newStatus);
                    
                    // 更新失败计数
                    if (result.isHealthy()) {
                        node.resetFailureCount();
                    } else {
                        node.incrementFailureCount();
                    }
                    
                    // 通知监听器
                    String serviceName = getServiceNameFromNode(node);
                    notifyListeners(listener -> listener.onNodeStatusChanged(
                        serviceName, node.getId(), oldStatus, newStatus));
                }
                
                node.updateLastHealthCheckTime(System.currentTimeMillis());
                
            } catch (Exception e) {
                log.error("节点健康检查失败: {}, 错误: {}", node.getId(), e.getMessage());
                node.incrementFailureCount();
            }
        }
    }
    
    /**
     * 通知监听器
     */
    private void notifyListeners(java.util.function.Consumer<ServiceChangeListener> action) {
        for (ServiceChangeListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("通知监听器失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getMonitorId() {
        return "";
    }

    @Override
    public MonitorType getMonitorType() {
        return null;
    }

    @Override
    public void registerMetrics(MetricsRegistry registry) {

    }

    @Override
    public MonitorMetadata getMonitorMetadata() {
        return null;
    }

    /**
     * 默认服务发现实现
     */
    private static class DefaultServiceDiscovery implements ServiceDiscovery {
        private volatile boolean running = false;
        
        @Override
        public List<ServiceNode> discoverNodes(String serviceName) {
            // 简单实现，返回空列表
            return new ArrayList<>();
        }
        
        @Override
        public CompletableFuture<List<ServiceNode>> discoverNodesAsync(String serviceName) {
            return CompletableFuture.supplyAsync(() -> discoverNodes(serviceName));
        }
        
        @Override
        public void registerNode(String serviceName, ServiceNode node) {
            log.debug("注册节点: {}/{}", serviceName, node.getId());
        }
        
        @Override
        public void unregisterNode(String serviceName, String nodeId) {
            log.debug("注销节点: {}/{}", serviceName, nodeId);
        }
        
        @Override
        public void updateNodeStatus(String serviceName, String nodeId, NodeStatus status) {
            log.debug("更新节点状态: {}/{} -> {}", serviceName, nodeId, status);
        }
        
        @Override
        public List<String> getAllServiceNames() {
            return new ArrayList<>();
        }
        
        @Override
        public void subscribeServiceChange(String serviceName, ServiceChangeListener listener) {
            // 简单实现
        }
        
        @Override
        public void unsubscribeServiceChange(String serviceName, ServiceChangeListener listener) {
            // 简单实现
        }

        @Override
        public void init() {

        }

        @Override
        public void start() {
            running = true;
            log.info("服务发现启动");
        }

        @Override
        public void shutdown() {
            running = false;
            log.info("服务发现停止");
        }
        
        @Override
        public boolean isRunning() {
            return running;
        }
    }
    
    /**
     * 默认健康检查器实现
     */
    private static class DefaultHealthChecker implements HealthChecker {
        private volatile boolean running = false;
        
        @Override
        public HealthCheckResult checkHealth(ServiceNode node) {
            try {
                // 模拟健康检查，这里简单返回成功
                // 实际实现中应该根据协议类型进行真实的健康检查
                return createHealthCheckResult(true, "节点健康");
            } catch (Exception e) {
                return createHealthCheckResult(false, "健康检查失败: " + e.getMessage());
            }
        }
        
        @Override
        public CompletableFuture<HealthCheckResult> checkHealthAsync(ServiceNode node) {
            return CompletableFuture.supplyAsync(() -> checkHealth(node));
        }
        
        @Override
        public void startScheduler() {
            running = true;
            log.info("健康检查调度器启动");
        }
        
        @Override
        public void stopScheduler() {
            running = false;
            log.info("健康检查调度器停止");
        }
        
        @Override
        public void addNode(ServiceNode node) {
            log.debug("添加健康检查节点: {}", node.getId());
        }
        
        @Override
        public void removeNode(String nodeId) {
            log.debug("移除健康检查节点: {}", nodeId);
        }
        
        @Override
        public Protocol getSupportedProtocol() {
            // 返回通用协议
            return new DefaultProtocol();
        }
    }
    
    /**
     * 默认协议实现
     */
    private static class DefaultProtocol implements Protocol {
        @Override
        public String getName() {
            return "DEFAULT";
        }
        
        @Override
        public String getVersion() {
            return "1.0";
        }
        
        @Override
        public ProtocolType getType() {
            return ProtocolType.CUSTOM;
        }
        
        @Override
        public boolean isConnectionOriented() {
            return true;
        }
        
        @Override
        public boolean isRequestResponseBased() {
            return true;
        }
        
        @Override
        public boolean isStreamingSupported() {
            return false;
        }
        
        @Override
        public int getDefaultPort() {
            return 8080;
        }
        
        @Override
        public java.util.Map<String, Object> getProtocolConfig() {
            return new HashMap<>();
        }
    }
    
    /**
     * 创建默认健康检查结果
     */
    private static HealthCheckResult createHealthCheckResult(boolean healthy, String message) {
        return new HealthCheckResult(healthy, message, healthy ? 10 : 1000);
    }
} 