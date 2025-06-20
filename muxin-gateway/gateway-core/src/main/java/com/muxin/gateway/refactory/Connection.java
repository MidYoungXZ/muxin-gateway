package com.muxin.gateway.refactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 连接接口 - 协议无关的连接抽象
 *
 * @author muxin
 */
public interface Connection {
    
    /**
     * 连接ID
     */
    String getConnectionId();
    
    /**
     * 协议信息
     */
    Protocol getProtocol();
    
    /**
     * 本地地址
     */
    EndpointAddress getLocalAddress();
    
    /**
     * 远程地址
     */
    EndpointAddress getRemoteAddress();
    
    /**
     * 连接状态
     */
    ConnectionStatus getStatus();
    
    /**
     * 发送消息
     */
    CompletableFuture<Void> send(Message message);
    
    /**
     * 关闭连接
     */
    CompletableFuture<Void> close();
    
    /**
     * 是否活跃
     */
    boolean isActive();
    
    /**
     * 连接属性
     */
    Map<String, Object> getAttributes();
    void setAttribute(String key, Object value);
    <T> T getAttribute(String key, Class<T> type);
    
    /**
     * 连接监听器
     */
    void addConnectionListener(ConnectionListener listener);
    void removeConnectionListener(ConnectionListener listener);
} 