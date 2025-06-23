package com.muxin.gateway.refactory.message;

import com.muxin.gateway.refactory.node.NodeStatus;
import com.muxin.gateway.refactory.node.UniversalServiceNode;

import java.util.List;

/**
 * 节点管理器接口
 * 负责服务节点的生命周期管理
 *
 * @author muxin
 */
public interface NodeManager {
    
    /**
     * 添加服务节点
     */
    void addNode(String serviceName, UniversalServiceNode node);
    
    /**
     * 移除服务节点
     */
    void removeNode(String serviceName, String nodeId);
    
    /**
     * 获取服务的所有节点
     */
    List<UniversalServiceNode> getNodes(String serviceName);
    
    /**
     * 获取指定节点
     */
    UniversalServiceNode getNode(String serviceName, String nodeId);
    
    /**
     * 获取服务的健康节点
     */
    List<UniversalServiceNode> getHealthyNodes(String serviceName);
    
    /**
     * 更新节点状态
     */
    void updateNodeStatus(String serviceName, String nodeId, NodeStatus status);
    
    /**
     * 获取所有服务名称
     */
    List<String> getAllServiceNames();
    
    /**
     * 启动节点管理器
     */
    void start();
    
    /**
     * 停止节点管理器
     */
    void stop();
} 