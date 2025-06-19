package com.muxin.gateway.core.registry;

import java.util.List;

/**
 * 注册中心变更监听器
 * 用于监听服务实例变更事件
 *
 * @author Administrator
 * @date 2025/6/13 18:00
 */
@FunctionalInterface
public interface RegisterCenterListener {

    /**
     * 当服务实例发生变更时的回调方法
     *
     * @param instances 变更后的服务实例列表
     */
    void onChange(List<ServiceInstance> instances);
} 