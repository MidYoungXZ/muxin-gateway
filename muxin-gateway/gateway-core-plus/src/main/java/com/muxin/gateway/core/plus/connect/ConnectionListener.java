package com.muxin.gateway.core.plus.connect;


import com.muxin.gateway.core.plus.message.Message;

/**
 * 连接监听器接口
 *
 * @author muxin
 */
public interface ConnectionListener {


    String id();

    /**
     * 连接建立事件
     */
    default void onConnectionEstablished(Connection connection) {
    }

    /**
     * 连接关闭事件
     */
    default void onConnectionClosed(Connection connection) {
    }

    /**
     * 连接错误事件
     */
    default void onConnectionError(Connection connection, Throwable error) {
    }

    /**
     * 消息接收事件
     */
    default void onMessageReceived(Connection connection, Message message) {
    }

    /**
     * 消息发送事件
     */
    default void onMessageSent(Connection connection, Message message) {
    }

    /**
     * 连接状态变更事件
     */
    default void onStatusChanged(Connection connection, ConnectionStatus oldStatus, ConnectionStatus newStatus) {
    }
} 