package com.muxin.gateway.core.plus.connect;

import java.util.concurrent.CompletableFuture;

/**
 * 连接接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface Connection {

    String getConnectionId();

    boolean isActive();

    CompletableFuture<Void> close();

    long getCreatedTime();

    long getLastActiveTime();
}
