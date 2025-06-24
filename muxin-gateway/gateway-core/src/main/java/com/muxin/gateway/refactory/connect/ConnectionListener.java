package com.muxin.gateway.refactory.connect;


import com.muxin.gateway.refactory.message.Message;

/**
 * 连接监听器接口
 *
 * @author muxin
 */
public interface ConnectionListener<R> {


    String id();

    /**
     * 连接建立事件
     */
    default void onConnectionEstablished(Connection<R> connection) {
    }

    /**
     * 连接关闭事件
     */
    default void onConnectionClosed(Connection<R> connection) {
    }

    /**
     * 连接错误事件
     */
    default void onConnectionError(Connection<R> connection, Throwable error) {
    }

    /**
     * 消息接收事件
     */
    default void onMessageReceived(Connection<R> connection, Message message) {
    }

    /**
     * 消息发送事件
     */
    default void onMessageSent(Connection<R> connection, Message message) {
    }

    /**
     * 连接状态变更事件
     */
    default void onStatusChanged(Connection<R> connection, ConnectionStatus oldStatus, ConnectionStatus newStatus) {
    }
} 