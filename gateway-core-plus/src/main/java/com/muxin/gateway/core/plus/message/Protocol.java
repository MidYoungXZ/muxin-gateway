package com.muxin.gateway.core.plus.message;

/**
 * 协议接口 - 定义协议的基本特征
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Protocol {
    /**
     * 协议名称 (HTTP, TCP, UDP, WebSocket, gRPC, MQTT等)
     */
    String type();

    /**
     * 协议版本
     */
    String getVersion();

    /**
     * 是否面向连接
     */
    boolean isConnectionOriented();

    /**
     * 是否支持请求-响应模式
     */
    boolean isRequestResponseBased();

    /**
     * 是否支持流式传输
     */
    boolean isStreamingSupported();

}