package com.muxin.gateway.refactory;

/**
 * 服务变化监听器接口
 *
 * @author muxin
 */
public interface ServiceChangeListener {
    
    /**
     * 服务节点添加事件
     */
    void onNodeAdded(String serviceName, UniversalServiceNode node);
    
    /**
     * 服务节点移除事件
     */
    void onNodeRemoved(String serviceName, String nodeId);
    
    /**
     * 服务节点状态变化事件
     */
    void onNodeStatusChanged(String serviceName, String nodeId, NodeStatus oldStatus, NodeStatus newStatus);
} 