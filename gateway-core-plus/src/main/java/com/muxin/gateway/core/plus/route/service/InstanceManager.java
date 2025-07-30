package com.muxin.gateway.core.plus.route.service;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.common.Repository;

import java.util.List;

/**
 * 节点管理器接口
 * 负责服务节点的生命周期管理
 *
 * @author muxin
 */
public interface InstanceManager extends Repository<String, ServiceInstance>, LifeCycle {

    /**
     * 获取服务的所有节点
     */
    List<ServiceInstance> getByServiceId(String serviceId);

    /**
     * 获取指定节点
     */
    ServiceInstance getByInstance(String serviceId, String instanceId);

    /**
     * 获取服务的健康节点
     */
    List<ServiceInstance> getHealthyInstances(String serviceId);

    /**
     * 更新节点状态
     */
    void updateInstanceStatus(String serviceName, String instanceId, NodeStatus status);

    /**
     * 获取所有服务ID
     */
    List<String> getAllServiceIds();

} 