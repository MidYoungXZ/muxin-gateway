package com.muxin.gateway.core.plus.connect;


/**
 * 连接状态枚举
 *
 * @author muxin
 */
public enum ConnectionStatus {
    
    /**
     * 连接中
     */
    CONNECTING,
    
    /**
     * 已连接
     */
    CONNECTED,
    
    /**
     * 断开中
     */
    DISCONNECTING,
    
    /**
     * 已断开
     */
    DISCONNECTED,
    
    /**
     * 错误状态
     */
    ERROR
} 