package com.muxin.gateway.refactory.node;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.LifeCycle;

import java.util.List;

/**
 * 节点管理器接口
 * 负责服务节点的生命周期管理
 *
 * @author muxin
 */
public interface NodeManager extends Repository<String, UniversalServiceNode>, LifeCycle {
    
    /**
     * 获取服务的所有节点
     */
    List<UniversalServiceNode> getNodes(String serviceName);
    
    /**
     * 获取指定节点
     */
    UniversalServiceNode getNode(String serviceName, String nodeId);
    
    /**
     * 获取服务的健康节点
     */
    List<UniversalServiceNode> getHealthyNodes(String serviceName);
    
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