package com.muxin.gateway.core.plus.route.service;

/**
 * 服务变化监听器接口
 *
 * @author muxin
 */
public interface ServiceChangeListener {

    /**
     * 服务节点添加事件
     */
    void onNodeAdded(String serviceId, ServiceInstance node);

    /**
     * 服务节点移除事件
     */
    void onNodeRemoved(String serviceId, String nodeId);

    /**
     * 服务节点状态变化事件
     */
    void onNodeStatusChanged(String serviceId, String nodeId, NodeStatus oldStatus, NodeStatus newStatus);
} 