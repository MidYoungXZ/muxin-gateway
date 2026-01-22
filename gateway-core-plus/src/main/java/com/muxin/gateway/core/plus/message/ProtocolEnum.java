package com.muxin.gateway.core.plus.message;

/**
 * 协议枚举
 * 定义网关支持的协议类型（v2.0）
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public enum ProtocolEnum implements Protocol {
    /**
     * HTTP协议
     */
    HTTP("HTTP", "1.1", true, true, false),
    
    /**
     * HTTP/2.0协议
     */
    HTTP_2_0("HTTP", "2.0", true, true, false),
    
    /**
     * gRPC协议
     */
    GRPC("GRPC", "1.0", true, true, true),
    
    /**
     * TCP协议
     */
    TCP("TCP", "1.0", false, false, false),
    
    /**
     * WebSocket协议
     */
    WEBSOCKET("WEBSOCKET", "13", true, false, true),
    
    /**
     * 负载均衡内部协议
     */
    LB("LB", "1.0", false, false, false);
    
    private final String code;
    private final String version;
    private final boolean isConnectionOriented;
    private final boolean isRequestResponseBased;
    private final boolean isStreamingSupported;
    
    ProtocolEnum(String code, String version, boolean isConnectionOriented, 
               boolean isRequestResponseBased, boolean isStreamingSupported) {
        this.code = code;
        this.version = version;
        this.isConnectionOriented = isConnectionOriented;
        this.isRequestResponseBased = isRequestResponseBased;
        this.isStreamingSupported = isStreamingSupported;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        // Protocol enum is immutable
        throw new UnsupportedOperationException("Cannot set code on immutable enum");
    }
    
    public void setVersion(String version) {
        // Protocol enum is immutable
        throw new UnsupportedOperationException("Cannot set version on immutable enum");
    }
    
    public void setConnectionOriented(boolean connectionOriented) {
        // Protocol enum is immutable
        throw new UnsupportedOperationException("Cannot set connectionOriented on immutable enum");
    }
    
    public void setRequestResponseBased(boolean requestResponseBased) {
        // Protocol enum is immutable
        throw new UnsupportedOperationException("Cannot set requestResponseBased on immutable enum");
    }
    
    public void setStreamingSupported(boolean streamingSupported) {
        // Protocol enum is immutable
        throw new UnsupportedOperationException("Cannot set streamingSupported on immutable enum");
    }
    
    @Override
    public String type() {
        return getCode();
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public boolean isConnectionOriented() {
        return isConnectionOriented;
    }
    
    @Override
    public boolean isRequestResponseBased() {
        return isRequestResponseBased;
    }
    
    @Override
    public boolean isStreamingSupported() {
        return isStreamingSupported;
    }
    
    /**
     * 根据代码获取协议枚举
     */
    public static ProtocolEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ProtocolEnum protocol : values()) {
            if (protocol.code.equalsIgnoreCase(code)) {
                return protocol;
            }
        }
        
        throw new IllegalArgumentException("不支持的协议类型: " + code);
    }
    
    /**
     * 根据代码和版本获取协议枚举
     */
    public static ProtocolEnum fromCode(String code, String version) {
        for (ProtocolEnum protocol : values()) {
            if (protocol.code.equalsIgnoreCase(code) && protocol.version.equals(version)) {
                return protocol;
            }
        }
        
        // 如果没有找到精确匹配，尝试只匹配code
        return fromCode(code);
    }
    
    /**
     * 是否为HTTP协议（包含HTTP/2.0）
     */
    public boolean isHttp() {
        return this == HTTP || this == HTTP_2_0;
    }
    
    /**
     * 是否为HTTP/2.0
     */
    public boolean isHttp2() {
        return this == HTTP_2_0;
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
    
    /**
     * 是否为负载均衡内部协议
     */
    public boolean isLb() {
        return this == LB;
    }
    
    /**
     * 获取协议的显示名称
     */
    public String getDisplayName() {
        switch (this) {
            case HTTP:
                return "HTTP/1.1";
            case HTTP_2_0:
                return "HTTP/2.0";
            case GRPC:
                return "gRPC";
            case TCP:
                return "TCP";
            case WEBSOCKET:
                return "WebSocket";
            case LB:
                return "Load Balance";
            default:
                return code;
        }
    }
    
    /**
     * 获取默认端口号
     */
    public int getDefaultPort() {
        switch (this) {
            case HTTP:
            case HTTP_2_0:
                return 80;
            case GRPC:
                return 50051;
            case TCP:
                return 8080;
            case WEBSOCKET:
                return 80;
            case LB:
                return 8080;
            default:
                return 8080;
        }
    }
    
    /**
     * 获取协议的安全传输方式
     */
    public String getSecureScheme() {
        switch (this) {
            case HTTP:
            case HTTP_2_0:
            case WEBSOCKET:
                return "https";
            case GRPC:
                return "grpcs";
            default:
                return null;
        }
    }
}