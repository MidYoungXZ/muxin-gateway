package com.muxin.gateway.core.plus.message.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Map;

/**
 * HTTP服务器交换接口
 * 定义HTTP服务器端请求和响应的交换上下文
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface HttpServerExchange {

    HttpRequestMessage request();

    HttpResponseMessage response();

    void setResponse(HttpResponseMessage response);

    FullHttpRequest nettyRequest();

    FullHttpResponse nettyResponse();

    void setNettyResponse(FullHttpResponse response);

    String getRequestBody();

    void setResponseBody(String body);

    String getResponseBody();

    void setAttribute(String key, Object value);

    Object getAttribute(String key);

    Map<String, Object> getAttributes();
}
