package com.muxin.gateway.core.common;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 响应状态码接口
 * 
 * 定义响应状态码的接口，包含HTTP状态码和内部状态码
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface ResponseStatusCode {

    HttpResponseStatus httpStatus();

    String internalStatus();

}
