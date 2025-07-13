package com.muxin.gateway.core.plus.common;

import com.muxin.gateway.core.plus.route.service.NodeStatus;
import com.muxin.gateway.core.plus.route.service.ServiceChangeListener;
import com.muxin.gateway.core.plus.route.service.ServiceInstance;

import java.util.List;

/**
 * 服务发现接口
 * 负责发现和管理后端服务节点
 *
 * @author muxin
 */
public interface ServiceRegistry extends LifeCycle {
    
    /**
     * 发现服务节点
     */
    List<ServiceInstance> selectInstances(String serviceName);

    /**
     * 注册服务节点
     */
    void register(ServiceInstance node);
    
    /**
     * 注销服务节点
     */
    void deregister(ServiceInstance  serviceInstance);
    
    /**
     * 更新服务节点状态
     *
     * @param serviceName 服务名称
     * @param nodeId 节点ID
     * @param status 新状态
     */
    void updateStatus(String serviceName, String nodeId, NodeStatus status);
    
    /**
     * 获取所有服务名称
     */
    List<String> getAllServiceIds();
    
    /**
     * 订阅服务变化事件
     *
     * @param serviceId 服务ID
     * @param listener 变化监听器
     */
    void subscribeServiceChange(String serviceId, ServiceChangeListener listener);
    
    /**
     * 取消订阅服务变化事件
     *
     * @param serviceId 服务ID
     * @param listener 变化监听器
     */
    void unsubscribeServiceChange(String serviceId, ServiceChangeListener listener);

    /**
     * 获取服务发现状态
     */
    boolean isRunning();
} 