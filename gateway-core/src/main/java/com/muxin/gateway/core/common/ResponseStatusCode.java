package com.muxin.gateway.core.common;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 14:14
 */
public interface ResponseStatusCode {

    HttpResponseStatus httpStatus();

    String internalStatus();

}
