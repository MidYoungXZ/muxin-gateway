package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 连接接口 - 协议无关的连接抽象
 *
 * @author muxin
 */
public interface Connection extends Repository<String, ConnectionListener> {

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
    CompletableFuture<Message> send(Message message);

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

    /**
     * 设置连接属性
     */
    void setAttribute(String key, Object value);

    /**
     * 获取连接属性
     */
    <T> T getAttribute(String key, Class<T> type);


    default void notifyListeners(Consumer<ConnectionListener> action) {
        for (ConnectionListener listener : findAll()) {
            action.accept(listener);
        }
    }


} 