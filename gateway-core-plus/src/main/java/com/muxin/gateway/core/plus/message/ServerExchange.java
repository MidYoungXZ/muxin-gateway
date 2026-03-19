package com.muxin.gateway.core.plus.message;

import com.muxin.gateway.core.plus.common.AttributesHolder;
import com.muxin.gateway.core.plus.message.http.HttpRequestMessage;
import com.muxin.gateway.core.plus.message.http.HttpResponseMessage;

/**
 * HTTP交换接口
 * 定义HTTP请求和响应的交换上下文，用于在网关处理过程中传递数据
 * 節化版本：只支持HTTP协议，移除泛型设计
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ServerExchange extends AttributesHolder {

    HttpRequestMessage request();

    HttpRequestMessage setRequest(HttpRequestMessage request);

    HttpResponseMessage response();

    HttpResponseMessage setResponse(HttpResponseMessage response);
}
