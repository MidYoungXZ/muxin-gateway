package com.muxin.gateway.core.http;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ServerWebExchange extends AttributesHolder {

    HttpServerRequest getRequest();

    HttpServerResponse getResponse();

    interface Builder {

        Builder request(HttpServerRequest request);

        Builder response(HttpServerResponse response);

        ServerWebExchange build();

    }

    Builder mutate();

}
