package com.muxin.gateway.core.plus.node;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;

import java.util.List;

/**
 * 节点管理器接口
 * 负责服务节点的生命周期管理
 *
 * @author muxin
 */
public interface NodeManager extends Repository<String, ServiceNode>, LifeCycle {

    /**
     * 获取服务的所有节点
     */
    List<ServiceNode> getNodes(String serviceName);

    /**
     * 获取指定节点
     */
    ServiceNode getNode(String serviceName, String nodeId);

    /**
     * 获取服务的健康节点
     */
    List<ServiceNode> getHealthyNodes(String serviceName);

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