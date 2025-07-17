package com.muxin.gateway.core.plus.connect;


import com.muxin.gateway.core.plus.message.Message;

import java.util.concurrent.CompletableFuture;

/**
 * 客户端连接接口
 * 网关作为客户端连接后端服务时使用，专注于请求发送功能
 * 
 * @author muxin
 */
public interface ClientConnection extends Connection {
    
    // ========== 客户端连接特有功能 ==========
    
    /**
     * 检查连接是否健康
     */
    boolean isHealthy();
    
    // ========== 请求发送 ==========
    
    /**
     * 发送请求到后端服务并等待响应
     * 
     * @param request 请求消息
     * @return 响应消息的Future
     */
    CompletableFuture<Message> send(Message request);
    
    // ========== 连接池相关 ==========
    
    /**
     * 标记连接为使用中
     */
    void markInUse();
    
    /**
     * 标记连接为空闲
     */
    void markIdle();
    
    /**
     * 检查连接是否在使用中
     */
    boolean isInUse();
    
    /**
     * 归还连接到池中
     */
    void returnToPool();
    
    /**
     * 销毁连接
     */
    void destroy();
    
    // ========== 客户端统计信息 ==========
    
    /**
     * 获取总请求数
     */
    long getTotalRequests();
    
    /**
     * 获取总失败数
     */
    long getTotalFailures();
} 