package com.muxin.gateway.core.service;

import com.muxin.gateway.core.common.LifeCycle;

import java.util.List;

/**
 * 统一的服务注册中心接口
 * 负责管理所有服务实例（静态配置 + 服务发现）
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ServiceRegistry extends LifeCycle {

    // ========== 实例查询 ==========

    /**
     * 获取服务的所有实例
     */
    List<ServiceInstance> getInstances(String serviceId);

    /**
     * 获取服务的健康实例
     */
    List<ServiceInstance> getHealthyInstances(String serviceId);

    /**
     * 获取指定实例
     */
    ServiceInstance getInstance(String serviceId, String instanceId);

    /**
     * 获取所有服务ID
     */
    List<String> getAllServiceIds();

    // ========== 实例注册/注销 ==========

    /**
     * 注册实例（支持静态配置和服务发现）
     */
    void registerInstance(ServiceInstance instance);

    /**
     * 注销实例
     */
    void deregisterInstance(String serviceId, String instanceId);

    // ========== 状态管理 ==========

    /**
     * 更新实例状态
     */
    void updateStatus(String serviceId, String instanceId, NodeStatus status);

    // ========== 变更订阅 ==========

    /**
     * 订阅实例变更事件
     */
    void subscribe(String serviceId, InstanceChangeListener listener);

    /**
     * 取消订阅实例变更事件
     */
    void unsubscribe(String serviceId, InstanceChangeListener listener);

    // ========== 运行状态 ==========

    /**
     * 获取运行状态
     */
    boolean isRunning();
}