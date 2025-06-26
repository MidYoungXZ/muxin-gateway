package com.muxin.gateway.refactory.node.health;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.UniversalServiceNode;

import java.util.concurrent.CompletableFuture;

/**
 * 健康检查器接口
 * 负责检查服务节点的健康状态
 *
 * @author muxin
 */
public interface HealthChecker {
    
    /**
     * 同步健康检查
     *
     * @param node 服务节点
     * @return 健康检查结果
     */
    HealthCheckResult checkHealth(UniversalServiceNode node);
    
    /**
     * 异步健康检查
     *
     * @param node 服务节点
     * @return 健康检查结果的Future
     */
    CompletableFuture<HealthCheckResult> checkHealthAsync(UniversalServiceNode node);
    
    /**
     * 启动健康检查调度器
     */
    void startScheduler();
    
    /**
     * 停止健康检查调度器
     */
    void stopScheduler();
    
    /**
     * 添加节点到健康检查列表
     */
    void addNode(UniversalServiceNode node);
    
    /**
     * 从健康检查列表移除节点
     */
    void removeNode(String nodeId);
    
    /**
     * 获取支持的协议
     */
    Protocol getSupportedProtocol();
} 