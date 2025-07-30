package com.muxin.gateway.core.registry;

import java.util.List;

/**
 * 注册中心接口
 * 定义了服务注册、发现、订阅等核心功能
 *
 * @author Administrator
 * @date 2025/6/13 18:00
 */
public interface RegisterCenter {

    /**
     * 注册服务实例
     *
     * @param instance 服务实例
     */
    void register(ServiceInstance instance);

    /**
     * 注销服务实例
     *
     * @param instance 服务实例
     */
    void deregister(ServiceInstance instance);

    /**
     * 查询服务实例列表
     *
     * @param serviceId 服务ID
     * @return 服务实例列表
     */
    List<ServiceInstance> selectInstances(String serviceId);

    /**
     * 查询健康的服务实例列表
     *
     * @param serviceId 服务ID
     * @param healthy   是否健康
     * @return 服务实例列表
     */
    List<ServiceInstance> selectInstances(String serviceId, Boolean healthy);

    /**
     * 订阅服务变更事件
     *
     * @param serviceId 服务ID
     * @param listener  变更监听器
     */
    void subscribe(String serviceId, RegisterCenterListener listener);

    /**
     * 取消订阅服务变更事件
     *
     * @param serviceId 服务ID
     * @param listener  变更监听器
     */
    void unsubscribe(String serviceId, RegisterCenterListener listener);

    /**
     * 检查注册中心是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 关闭注册中心连接
     */
    void shutdown();
} 