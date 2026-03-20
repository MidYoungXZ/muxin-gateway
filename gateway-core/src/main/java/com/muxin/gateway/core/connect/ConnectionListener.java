package com.muxin.gateway.core.connect;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * 连接监听器接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ConnectionListener {

    String id();

    default void onConnectionEstablished(Connection connection) {
    }

    default void onConnectionClosed(Connection connection) {
    }

    default void onConnectionError(Connection connection, Throwable error) {
    }

    default void onRequestSent(Connection connection, FullHttpRequest request) {
    }

    default void onResponseReceived(Connection connection, FullHttpResponse response) {
    }

    default void onStatusChanged(Connection connection, ConnectionStatus oldStatus, ConnectionStatus newStatus) {
    }
}
