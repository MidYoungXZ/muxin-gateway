package com.muxin.gateway.refactory;

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
public class DefaultNodeManager implements NodeManager {
    
    private final Map<String, Map<String, UniversalServiceNode>> serviceNodes;
    private final ServiceDiscovery serviceDiscovery;
    private final HealthChecker healthChecker;
    private final ScheduledExecutorService scheduler;
    private final List<ServiceChangeListener> listeners;
    private volatile boolean running = false;
    
    public DefaultNodeManager() {
        this.serviceNodes = new ConcurrentHashMap<>();
        this.serviceDiscovery = new DefaultServiceDiscovery();
        this.healthChecker = new DefaultHealthChecker();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listeners = new ArrayList<>();
    }
    
    @Override
    public void addNode(String serviceName, UniversalServiceNode node) {
        if (serviceName != null && node != null && node.getId() != null) {
            serviceNodes.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                       .put(node.getId(), node);
            System.out.println("[NODE_MANAGER] 添加节点: " + serviceName + "/" + node.getId() + " - " + node.getAddress().toUri());
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeAdded(serviceName, node));
        }
    }
    
    @Override
    public void removeNode(String serviceName, String nodeId) {
        Map<String, UniversalServiceNode> nodes = serviceNodes.get(serviceName);
        if (nodes != null) {
            UniversalServiceNode removed = nodes.remove(nodeId);
            if (removed != null) {
                System.out.println("[NODE_MANAGER] 移除节点: " + serviceName + "/" + nodeId);
                
                // 通知监听器
                notifyListeners(listener -> listener.onNodeRemoved(serviceName, nodeId));
                
                // 如果服务没有节点了，移除服务
                if (nodes.isEmpty()) {
                    serviceNodes.remove(serviceName);
                }
            }
        }
    }
    
    @Override
    public List<UniversalServiceNode> getNodes(String serviceName) {
        Map<String, UniversalServiceNode> nodes = serviceNodes.get(serviceName);
        return nodes != null ? new ArrayList<>(nodes.values()) : new ArrayList<>();
    }
    
    @Override
    public UniversalServiceNode getNode(String serviceName, String nodeId) {
        Map<String, UniversalServiceNode> nodes = serviceNodes.get(serviceName);
        return nodes != null ? nodes.get(nodeId) : null;
    }
    
    @Override
    public List<UniversalServiceNode> getHealthyNodes(String serviceName) {
        return getNodes(serviceName).stream()
                .filter(UniversalServiceNode::isHealthy)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateNodeStatus(String serviceName, String nodeId, NodeStatus status) {
        UniversalServiceNode node = getNode(serviceName, nodeId);
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
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        // 启动健康检查
        startHealthCheck();
        
        // 启动服务发现
        serviceDiscovery.start();
        
        System.out.println("[NODE_MANAGER] 节点管理器启动完成");
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
        serviceDiscovery.stop();
        
        System.out.println("[NODE_MANAGER] 节点管理器停止完成");
    }
    
    /**
     * 获取所有节点（用于兼容）
     */
    public List<UniversalServiceNode> getAllNodes() {
        return serviceNodes.values().stream()
                .flatMap(nodes -> nodes.values().stream())
                .collect(Collectors.toList());
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
                System.err.println("[NODE_MANAGER] 健康检查失败: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        for (String serviceName : serviceNodes.keySet()) {
            Map<String, UniversalServiceNode> nodes = serviceNodes.get(serviceName);
            if (nodes != null) {
                for (UniversalServiceNode node : nodes.values()) {
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
                            notifyListeners(listener -> listener.onNodeStatusChanged(
                                serviceName, node.getId(), oldStatus, newStatus));
                        }
                        
                        node.updateLastHealthCheckTime(System.currentTimeMillis());
                        
                    } catch (Exception e) {
                        System.err.println("[NODE_MANAGER] 节点健康检查失败: " + serviceName + "/" + node.getId() + ", 错误: " + e.getMessage());
                        node.incrementFailureCount();
                    }
                }
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
                System.err.println("[NODE_MANAGER] 通知监听器失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取节点统计信息
     */
    public Map<String, Object> getStatistics() {
        List<UniversalServiceNode> allNodes = getAllNodes();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServices", serviceNodes.size());
        stats.put("totalNodes", allNodes.size());
        stats.put("healthyNodes", allNodes.stream().mapToInt(node -> node.isHealthy() ? 1 : 0).sum());
        
        // 按状态分组统计
        Map<NodeStatus, Long> statusStats = allNodes.stream()
                .collect(Collectors.groupingBy(
                    UniversalServiceNode::getStatus,
                    Collectors.counting()
                ));
        stats.put("statusDistribution", statusStats);
        
        return stats;
    }
    
    /**
     * 默认服务发现实现
     */
    private static class DefaultServiceDiscovery implements ServiceDiscovery {
        private volatile boolean running = false;
        
        @Override
        public List<UniversalServiceNode> discoverNodes(String serviceName) {
            // 简单实现，返回空列表
            return new ArrayList<>();
        }
        
        @Override
        public CompletableFuture<List<UniversalServiceNode>> discoverNodesAsync(String serviceName) {
            return CompletableFuture.supplyAsync(() -> discoverNodes(serviceName));
        }
        
        @Override
        public void registerNode(String serviceName, UniversalServiceNode node) {
            System.out.println("[SERVICE_DISCOVERY] 注册节点: " + serviceName + "/" + node.getId());
        }
        
        @Override
        public void unregisterNode(String serviceName, String nodeId) {
            System.out.println("[SERVICE_DISCOVERY] 注销节点: " + serviceName + "/" + nodeId);
        }
        
        @Override
        public void updateNodeStatus(String serviceName, String nodeId, NodeStatus status) {
            System.out.println("[SERVICE_DISCOVERY] 更新节点状态: " + serviceName + "/" + nodeId + " -> " + status);
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
        public void start() {
            running = true;
            System.out.println("[SERVICE_DISCOVERY] 服务发现启动");
        }
        
        @Override
        public void stop() {
            running = false;
            System.out.println("[SERVICE_DISCOVERY] 服务发现停止");
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
        public HealthCheckResult checkHealth(UniversalServiceNode node) {
            try {
                // 模拟健康检查，这里简单返回成功
                // 实际实现中应该根据协议类型进行真实的健康检查
                return createHealthCheckResult(true, "节点健康");
            } catch (Exception e) {
                return createHealthCheckResult(false, "健康检查失败: " + e.getMessage());
            }
        }
        
        @Override
        public CompletableFuture<HealthCheckResult> checkHealthAsync(UniversalServiceNode node) {
            return CompletableFuture.supplyAsync(() -> checkHealth(node));
        }
        
        @Override
        public void startScheduler() {
            running = true;
            System.out.println("[HEALTH_CHECKER] 健康检查调度器启动");
        }
        
        @Override
        public void stopScheduler() {
            running = false;
            System.out.println("[HEALTH_CHECKER] 健康检查调度器停止");
        }
        
        @Override
        public void addNode(UniversalServiceNode node) {
            System.out.println("[HEALTH_CHECKER] 添加健康检查节点: " + node.getId());
        }
        
        @Override
        public void removeNode(String nodeId) {
            System.out.println("[HEALTH_CHECKER] 移除健康检查节点: " + nodeId);
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