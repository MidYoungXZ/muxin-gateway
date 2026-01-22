package com.muxin.gateway.core.plus.route;

/**
 * 协议类型枚举
 * 定义网关支持的协议类型
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ProtocolType {
    
    /**
     * HTTP协议
     */
    HTTP("HTTP", "超文本传输协议"),
    
    /**
     * gRPC协议
     */
    GRPC("GRPC", "Google远程过程调用"),
    
    /**
     * TCP协议
     */
    TCP("TCP", "传输控制协议"),
    
    /**
     * WebSocket协议
     */
    WEBSOCKET("WEBSOCKET", "Web套接字协议");
    
    private final String code;
    private final String description;
    
    ProtocolType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取协议类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取协议类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取协议类型
     */
    public static ProtocolType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ProtocolType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("不支持的协议类型: " + code);
    }
    
    /**
     * 是否为HTTP协议
     */
    public boolean isHttp() {
        return this == HTTP;
    }
    
    /**
     * 是否为gRPC协议
     */
    public boolean isGrpc() {
        return this == GRPC;
    }
    
    /**
     * 是否为TCP协议
     */
    public boolean isTcp() {
        return this == TCP;
    }
    
    /**
     * 是否为WebSocket协议
     */
    public boolean isWebSocket() {
        return this == WEBSOCKET;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}