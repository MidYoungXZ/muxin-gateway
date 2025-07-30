package com.muxin.gateway.core.common;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 14:19
 */
@Getter
public enum ResponseStatusEnum implements ResponseStatusCode{

    //00-00-0000 模块-错误类型-具体错误码

    G00_00_0000(HttpResponseStatus.OK,"00-00-0000"),
    G00_04_0004(HttpResponseStatus.NOT_FOUND,"00-04-0004"),
    G00_05_0005(HttpResponseStatus.INTERNAL_SERVER_ERROR,"00-05-0005"),
    ;

    ResponseStatusEnum(HttpResponseStatus httpResponseStatus, String internalStatus) {
        this.httpResponseStatus = httpResponseStatus;
        this.internalStatus = internalStatus;
    }

    private final HttpResponseStatus httpResponseStatus;

    private final String internalStatus;


    @Override
    public HttpResponseStatus httpStatus() {
        return this.getHttpResponseStatus();
    }

    @Override
    public String internalStatus() {
        return this.getInternalStatus();
    }
}
