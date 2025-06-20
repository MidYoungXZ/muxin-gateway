package com.muxin.gateway.refactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务发现接口
 * 负责发现和管理后端服务节点
 *
 * @author muxin
 */
public interface ServiceDiscovery {
    
    /**
     * 发现服务节点
     */
    List<UniversalServiceNode> discoverNodes(String serviceName);
    
    /**
     * 异步发现服务节点
     *
     * @param serviceName 服务名称
     * @return 服务节点列表的Future
     */
    CompletableFuture<List<UniversalServiceNode>> discoverNodesAsync(String serviceName);
    
    /**
     * 注册服务节点
     */
    void registerNode(String serviceName, UniversalServiceNode node);
    
    /**
     * 注销服务节点
     */
    void unregisterNode(String serviceName, String nodeId);
    
    /**
     * 更新服务节点状态
     *
     * @param serviceName 服务名称
     * @param nodeId 节点ID
     * @param status 新状态
     */
    void updateNodeStatus(String serviceName, String nodeId, NodeStatus status);
    
    /**
     * 获取所有服务名称
     */
    List<String> getAllServiceNames();
    
    /**
     * 订阅服务变化事件
     *
     * @param serviceName 服务名称
     * @param listener 变化监听器
     */
    void subscribeServiceChange(String serviceName, ServiceChangeListener listener);
    
    /**
     * 取消订阅服务变化事件
     *
     * @param serviceName 服务名称
     * @param listener 变化监听器
     */
    void unsubscribeServiceChange(String serviceName, ServiceChangeListener listener);
    
    /**
     * 启动服务发现
     */
    void start();
    
    /**
     * 停止服务发现
     */
    void stop();
    
    /**
     * 获取服务发现状态
     */
    boolean isRunning();
} 