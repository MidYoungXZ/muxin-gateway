package com.muxin.gateway.refactory;


/**
 * 协议类型枚举
 *
 * @author muxin
 */
public enum ProtocolType {
    
    /**
     * HTTP协议 (HTTP/1.1, HTTP/2, HTTP/3)
     */
    HTTP,
    
    /**
     * 原始TCP协议
     */
    TCP,
    
    /**
     * 原始UDP协议
     */
    UDP,
    
    /**
     * WebSocket协议
     */
    WEBSOCKET,
    
    /**
     * gRPC协议 (基于HTTP/2)
     */
    GRPC,
    
    /**
     * MQTT消息队列协议
     */
    MQTT,
    
    /**
     * Redis协议
     */
    REDIS,
    
    /**
     * MySQL协议
     */
    MYSQL,
    
    /**
     * 自定义协议
     */
    CUSTOM
} 