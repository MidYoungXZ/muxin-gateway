package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;

/**
 * 统一的HTTP交换接口，同时提供请求和响应的功能
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ServerWebExchange extends HttpServerRequest, HttpServerResponse, ReleaseAble, AttributesHolder {

    /**
     * 获取http请求对象（兼容性方法）
     *
     * @return HttpServerRequest
     */
    HttpServerRequest getRequest();

    /**
     * 获取http响应对象（兼容性方法）
     *
     * @return HttpServerResponse
     */
    HttpServerResponse getResponse();

    /**
     * 设置http响应对象（兼容性方法）
     *
     * @param response Object
     */
    void setOriginalResponse(Object response);



    /**
     * 设置http请求对象（兼容性方法）
     *
     * @param request Object
     */
    void setOriginalRequest(Object request);


    /**
     * Get the underlying Netty response
     *
     * @return getOriginalResponse
     */
    <T> T getOriginalResponse();

    /**
     * Get the underlying Netty response
     *
     * @return getOriginalResponse
     */
    <T> T getOriginalRequest();


    /**
     * netty进站数据
     *
     * @return ChannelHandlerContext
     */
    ChannelHandlerContext inboundContext();

    /**
     * 请求处理阶段
     *
     * @return ProcessingPhase
     */
    ProcessingPhase processingPhase();

    /**
     * 设置请求处理阶段
     *
     * @param phase ProcessingPhase
     */
    void setProcessingPhase(ProcessingPhase phase);
}
