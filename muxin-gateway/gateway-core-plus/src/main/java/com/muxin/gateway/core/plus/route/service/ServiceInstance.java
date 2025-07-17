package com.muxin.gateway.core.plus.route.service;

import java.util.Map;

/**
 * 协议无关的服务节点接口
 * 表示网关后端的一个服务实例
 *
 * @author muxin
 */
public interface ServiceInstance {

    ServiceMeta serviceMeta();
    /**
     * 获取节点唯一标识
     */
    String instanceId();

    /**
     * 获取节点地址
     */
    EndpointAddress getAddress();
    
    /**
     * 获取节点状态
     */
    NodeStatus getStatus();
    
    /**
     * 更新节点状态
     */
    void updateStatus(NodeStatus status);
    
    /**
     * 获取节点元数据
     */
    Map<String, Object> getMetadata();
    /**
     * 判断节点是否健康
     */
    boolean isHealthy();

} 