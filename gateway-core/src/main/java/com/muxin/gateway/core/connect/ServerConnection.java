package com.muxin.gateway.core.connect;

import io.netty.handler.codec.http.FullHttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP服务端连接接口
 * 网关作为服务端接收客户端请求时使用
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ServerConnection extends Connection {

    CompletableFuture<Void> sendResponse(FullHttpResponse response);

    CompletableFuture<Void> sendError(Throwable error);

    String getClientAddress();

    String getRemoteHost();

    int getRemotePort();
}
