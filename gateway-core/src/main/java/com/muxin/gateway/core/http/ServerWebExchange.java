package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ServerWebExchange extends AttributesHolder {

    /**
     * 获取http请求
     * @return
     */
    HttpServerRequest getRequest();

    /**
     * 获取http响应
     * @return
     */
    HttpServerResponse getResponse();

    /**
     * 设置http请求
     * @param response
     */
    void setResponse(HttpServerResponse response);

    /**
     * netty进站数据
     * @return
     */
    ChannelHandlerContext inboundContext();

    /**
     * 请求处理阶段
     * @return
     */
    ProcessingPhase processingPhase();

}
