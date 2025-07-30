package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.message.Protocol;

import java.util.concurrent.CompletableFuture;

/**
 * 连接接口
 * 定义所有类型连接的通用行为
 *
 * @author muxin
 */
public interface Connection {

    // ========== 基础连接信息 ==========

    /**
     * 获取连接ID
     */
    String getConnectionId();

    /**
     * 检查连接是否活跃
     */
    boolean isActive();

    /**
     * 关闭连接
     */
    CompletableFuture<Void> close();

    // ========== 连接属性管理 ==========

    /**
     * 获取连接创建时间
     */
    long getCreatedTime();

    /**
     * 获取最后活跃时间
     * 对于ClientConnection是最后使用时间，对于ServerConnection是最后活跃时间
     */
    long getLastActiveTime();

    /**
     * 获取连接协议
     */
    Protocol getProtocol();
}