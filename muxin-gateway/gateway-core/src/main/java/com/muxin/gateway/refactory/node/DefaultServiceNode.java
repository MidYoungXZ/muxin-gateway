package com.muxin.gateway.refactory.node;

import com.muxin.gateway.refactory.node.health.HealthCheckConfig;
import com.muxin.gateway.refactory.Protocol;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认服务节点实现
 * 
 * @author muxin
 */
public class DefaultServiceNode implements UniversalServiceNode {
    
    private final String id;
    private final String name;
    private final EndpointAddress address;
    private final Protocol protocol;
    private final int weight;
    private final Map<String, Object> metadata;
    private final HealthCheckConfig healthCheckConfig;
    private final long createdTime;
    
    private volatile NodeStatus status;
    private final AtomicLong lastHealthCheckTime;
    private final AtomicLong lastActiveTime;
    private final AtomicInteger failureCount;
    private final Duration connectionTimeout;
    private final Duration readTimeout;
    
    public DefaultServiceNode(String id, String name, EndpointAddress address, Protocol protocol) {
        this(id, name, address, protocol, 100); // 默认权重100
    }
    
    public DefaultServiceNode(String id, String name, EndpointAddress address, Protocol protocol, int weight) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.protocol = protocol;
        this.weight = weight;
        this.metadata = new ConcurrentHashMap<>();
        this.status = NodeStatus.STARTING;
        this.lastHealthCheckTime = new AtomicLong(0);
        this.lastActiveTime = new AtomicLong(System.currentTimeMillis());
        this.failureCount = new AtomicInteger(0);
        this.createdTime = System.currentTimeMillis();
        
        // 默认健康检查配置
        this.healthCheckConfig = new DefaultHealthCheckConfig();
        
        // 默认超时配置
        this.connectionTimeout = Duration.ofSeconds(5);
        this.readTimeout = Duration.ofSeconds(30);
        
        // 初始化为健康状态
        this.status = NodeStatus.HEALTHY;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public EndpointAddress getAddress() {
        return address;
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }
    
    @Override
    public NodeStatus getStatus() {
        return status;
    }
    
    @Override
    public void updateStatus(NodeStatus status) {
        NodeStatus oldStatus = this.status;
        this.status = status;
        
        if (oldStatus != status) {
            System.out.println(String.format("[SERVICE_NODE] 节点状态变更: %s %s -> %s", 
                id, oldStatus.getDescription(), status.getDescription()));
        }
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }
    
    @Override
    public HealthCheckConfig getHealthCheckConfig() {
        return healthCheckConfig;
    }
    
    @Override
    public long getLastHealthCheckTime() {
        return lastHealthCheckTime.get();
    }
    
    @Override
    public void updateLastHealthCheckTime(long timestamp) {
        lastHealthCheckTime.set(timestamp);
    }
    
    @Override
    public int getFailureCount() {
        return failureCount.get();
    }
    
    @Override
    public void incrementFailureCount() {
        int count = failureCount.incrementAndGet();
        System.out.println(String.format("[SERVICE_NODE] 节点 %s 失败次数增加至: %d", id, count));
        
        // 如果失败次数过多，标记为不健康
        if (count >= 3) {
            updateStatus(NodeStatus.UNHEALTHY);
        }
    }
    
    @Override
    public void resetFailureCount() {
        int oldCount = failureCount.getAndSet(0);
        if (oldCount > 0) {
            System.out.println(String.format("[SERVICE_NODE] 节点 %s 失败次数重置", id));
            
            // 失败次数重置时，如果节点状态是不健康，恢复为健康状态
            if (status == NodeStatus.UNHEALTHY) {
                updateStatus(NodeStatus.HEALTHY);
            }
        }
    }
    
    @Override
    public long getCreatedTime() {
        return createdTime;
    }
    
    @Override
    public long getLastActiveTime() {
        return lastActiveTime.get();
    }
    
    @Override
    public void updateLastActiveTime() {
        lastActiveTime.set(System.currentTimeMillis());
    }
    
    @Override
    public boolean isAvailable() {
        return status.isAvailable();
    }
    
    @Override
    public boolean isHealthy() {
        return status.isHealthy();
    }
    
    @Override
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
    
    @Override
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * 获取运行时间（毫秒）
     */
    public long getUptime() {
        return System.currentTimeMillis() - createdTime;
    }
    
    /**
     * 检查节点是否长时间未活跃
     */
    public boolean isInactive(long inactiveThresholdMs) {
        return System.currentTimeMillis() - lastActiveTime.get() > inactiveThresholdMs;
    }
    
    @Override
    public String toString() {
        return "DefaultServiceNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address=" + address +
                ", protocol=" + protocol +
                ", weight=" + weight +
                ", status=" + status +
                ", failureCount=" + failureCount.get() +
                ", uptime=" + getUptime() +
                '}';
    }
    
    /**
     * 默认健康检查配置实现
     */
    private static class DefaultHealthCheckConfig implements HealthCheckConfig {
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public Duration getInterval() {
            return Duration.ofSeconds(30); // 30秒
        }
        
        @Override
        public Duration getTimeout() {
            return Duration.ofSeconds(5); // 5秒
        }
        
        @Override
        public String getPath() {
            return "/health";
        }
        
        @Override
        public java.util.List<Integer> getExpectedStatusCodes() {
            return java.util.Arrays.asList(200, 201, 202, 204);
        }
        
        @Override
        public int getFailureThreshold() {
            return 3;
        }
        
        @Override
        public int getSuccessThreshold() {
            return 2;
        }
    }
} 