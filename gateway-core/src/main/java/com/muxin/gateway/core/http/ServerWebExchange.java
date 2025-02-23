package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;

/**
 * 定义了一个处理HTTP服务器请求和响应的接口。该接口提供了访问HTTP请求、HTTP响应、Netty进站数据和请求处理阶段的方法。
 * 所有具体的HTTP请求和响应对象都需要实现这个接口。
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ServerWebExchange extends AttributesHolder {

    /**
     * 获取HTTP请求对象。
     *
     * @return 当前的 {@link HttpServerRequest} 对象
     */
    HttpServerRequest getRequest();

    /**
     * 获取HTTP响应对象。
     *
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse getResponse();

    /**
     * 获取Netty进站数据的上下文。
     *
     * @return 当前的 {@link ChannelHandlerContext} 对象
     */
    ChannelHandlerContext inboundContext();

    /**
     * 获取请求处理阶段。
     *
     * @return 当前的 {@link ProcessingPhase} 对象
     */
    ProcessingPhase processingPhase();
}
