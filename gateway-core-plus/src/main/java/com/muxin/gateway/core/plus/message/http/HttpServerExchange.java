package com.muxin.gateway.core.plus.message.http;

import com.muxin.gateway.core.plus.message.ServerExchange;

/**
 * HTTP服务器交换接口
 * 定义HTTP服务器端请求和响应的交换上下文
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpServerExchange extends ServerExchange<HttpRequestMessage, HttpResponseMessage> {

}
