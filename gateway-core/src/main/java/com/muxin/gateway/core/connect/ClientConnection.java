package com.muxin.gateway.core.connect;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP客户端连接接口
 * 网关作为客户端连接后端服务时使用
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ClientConnection extends Connection {

    boolean isHealthy();

    CompletableFuture<FullHttpResponse> send(FullHttpRequest request);

    void markInUse();

    void markIdle();

    boolean isInUse();

    void returnToPool();

    void destroy();

    long getTotalRequests();

    long getTotalFailures();
}
