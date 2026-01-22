package com.muxin.gateway.core.plus.message.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * HTTP响应消息接口
 * 定义HTTP响应消息的接口，包含状态码和连接保持设置
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpResponseMessage extends HttpMessage {

    HttpResponseStatus status();

    void setStatus(HttpResponseStatus httpResponseStatus);

    HttpResponseMessage keepAlive(boolean keepAlive);
}
