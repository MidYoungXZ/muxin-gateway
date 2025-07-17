package com.muxin.gateway.core.plus.message;


/**
 * 消息类型枚举
 *
 * @author muxin
 */
public enum MessageType {
    
    /**
     * 请求消息
     */
    REQUEST,
    
    /**
     * 响应消息
     */
    RESPONSE,
    
    /**
     * 通知消息
     */
    NOTIFICATION,
    
    /**
     * 心跳消息
     */
    HEARTBEAT,
    
    /**
     * 流数据消息
     */
    STREAM_DATA,
    
    /**
     * 控制消息
     */
    CONTROL
} 