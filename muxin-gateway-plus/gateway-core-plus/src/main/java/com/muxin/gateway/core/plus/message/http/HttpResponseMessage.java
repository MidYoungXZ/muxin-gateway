package com.muxin.gateway.core.plus.message.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @projectname: muxin-gateway
 * @filename: HttpResponseMessage
 * @author: yangxz
 * @data:2025/7/16 21:23
 * @description:
 */
public interface HttpResponseMessage extends HttpMessage {

    HttpResponseStatus status();

    void setStatus(HttpResponseStatus httpResponseStatus);

    HttpResponseMessage keepAlive(boolean keepAlive);
}
