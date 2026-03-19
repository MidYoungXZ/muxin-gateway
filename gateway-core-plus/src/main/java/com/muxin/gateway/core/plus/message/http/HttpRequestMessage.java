package com.muxin.gateway.core.plus.message.http;

 
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * HTTP请求消息接口
 * 定义HTTP请求消息的接口，包含请求方法、URI等请求特定属性
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface HttpRequestMessage extends HttpMessage {

 
    HttpMethod method();
 
    void setMethod(HttpMethod httpMethod);
 
    String uri();
 
    String fullPath();
 
    String requestId();
 
    boolean isKeepAlive();
 
    String param(CharSequence key);
 
    Map<String, String> params();
 
    ZonedDateTime timestamp();
}
