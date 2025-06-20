package com.muxin.gateway.refactory;

import java.time.Duration;
import java.util.Map;

/**
 * 协议无关的服务节点接口
 * 表示网关后端的一个服务实例
 *
 * @author muxin
 */
public interface UniversalServiceNode {
    
    /**
     * 获取节点唯一标识
     */
    String getId();
    
    /**
     * 获取节点名称
     */
    String getName();
    
    /**
     * 获取节点地址
     */
    EndpointAddress getAddress();
    
    /**
     * 获取节点支持的协议
     */
    Protocol getProtocol();
    
    /**
     * 获取节点权重 (用于负载均衡)
     */
    int getWeight();
    
    /**
     * 获取节点状态
     */
    NodeStatus getStatus();
    
    /**
     * 更新节点状态
     */
    void updateStatus(NodeStatus status);
    
    /**
     * 获取节点元数据
     */
    Map<String, Object> getMetadata();
    
    /**
     * 获取健康检查配置
     */
    HealthCheckConfig getHealthCheckConfig();
    
    /**
     * 获取最后健康检查时间
     */
    long getLastHealthCheckTime();
    
    /**
     * 更新最后健康检查时间
     */
    void updateLastHealthCheckTime(long timestamp);
    
    /**
     * 获取连续失败次数
     */
    int getFailureCount();
    
    /**
     * 增加失败次数
     */
    void incrementFailureCount();
    
    /**
     * 重置失败次数
     */
    void resetFailureCount();
    
    /**
     * 获取节点创建时间
     */
    long getCreatedTime();
    
    /**
     * 获取节点最后活跃时间
     */
    long getLastActiveTime();
    
    /**
     * 更新最后活跃时间
     */
    void updateLastActiveTime();
    
    /**
     * 判断节点是否可用
     */
    boolean isAvailable();
    
    /**
     * 判断节点是否健康
     */
    boolean isHealthy();
    
    /**
     * 获取连接超时时间
     */
    Duration getConnectionTimeout();
    
    /**
     * 获取读取超时时间
     */
    Duration getReadTimeout();
} 