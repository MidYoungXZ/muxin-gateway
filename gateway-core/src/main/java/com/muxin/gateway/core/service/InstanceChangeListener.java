package com.muxin.gateway.core.service;

/**
 * 实例变更监听器接口
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface InstanceChangeListener {

    /**
     * 实例添加事件
     */
    void onInstanceAdded(String serviceId, ServiceInstance instance);

    /**
     * 实例移除事件
     */
    void onInstanceRemoved(String serviceId, String instanceId);

    /**
     * 实例状态变化事件
     */
    void onInstanceStatusChanged(String serviceId, String instanceId, NodeStatus oldStatus, NodeStatus newStatus);
}