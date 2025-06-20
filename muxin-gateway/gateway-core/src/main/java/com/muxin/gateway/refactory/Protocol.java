package com.muxin.gateway.refactory;

import java.util.Map;

/**
 * 协议接口 - 定义协议的基本特征
 *
 * @author muxin
 */
public interface Protocol {
    
    /**
     * 协议名称 (HTTP, TCP, UDP, WebSocket, gRPC, MQTT等)
     */
    String getName();
    
    /**
     * 协议版本
     */
    String getVersion();
    
    /**
     * 协议类型
     */
    ProtocolType getType();
    
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
    
    /**
     * 默认端口
     */
    int getDefaultPort();
    
    /**
     * 协议特定配置
     */
    Map<String, Object> getProtocolConfig();
} 