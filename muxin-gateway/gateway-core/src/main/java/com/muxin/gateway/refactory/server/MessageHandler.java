package com.muxin.gateway.refactory.server;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;

import java.util.concurrent.CompletableFuture;

/**
 * 消息处理器接口
 * 定义如何处理来自各种协议的消息
 * 
 * @author muxin
 */
public interface MessageHandler {
    
    /**
     * 处理入站消息
     * 
     * @param message 入站消息
     * @param connection 连接对象
     * @return 响应消息
     */
    CompletableFuture<Message> handleMessage(Message message, Connection connection);
    
    /**
     * 处理连接建立事件
     * 
     * @param connection 新建立的连接
     */
    default void onConnectionEstablished(Connection connection) {
        // 默认空实现
    }
    
    /**
     * 处理连接关闭事件
     * 
     * @param connection 即将关闭的连接
     */
    default void onConnectionClosed(Connection connection) {
        // 默认空实现
    }
    
    /**
     * 处理异常事件
     * 
     * @param connection 发生异常的连接
     * @param cause 异常原因
     */
    default void onException(Connection connection, Throwable cause) {
        // 默认空实现
    }
} 