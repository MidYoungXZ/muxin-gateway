package com.muxin.gateway.core.http;

import io.netty.channel.ChannelHandlerContext;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ServerWebExchange extends AttributesHolder {

    HttpServerRequest getRequest();

    HttpServerResponse getResponse();

    void setResponse(HttpServerResponse response);

    ChannelHandlerContext inboundContext();

    interface Builder {

        Builder request(HttpServerRequest request);

        Builder response(HttpServerResponse response);

        ServerWebExchange build();

    }

    Builder mutate();

}
