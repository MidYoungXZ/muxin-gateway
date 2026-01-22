package com.muxin.gateway.core.plus.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议配置类（v2.0）
 * 支持单协议配置
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolDefinition {
    
    /**
     * 协议类型
     * 对应 ProtocolType 枚举的 code
     */
    private String type;
    
    /**
     * 协议版本
     */
    @Builder.Default
    private String version = "1.1";
    
    /**
     * 转换为ProtocolEnum对象
     */
    public ProtocolEnum toProtocol() {
        return ProtocolEnum.fromCode(type, version);
    }
    
    /**
     * 从ProtocolEnum对象创建ProtocolDefinition
     */
    public static ProtocolDefinition fromProtocol(Protocol protocol) {
        if (protocol instanceof ProtocolEnum) {
            ProtocolEnum protocolEnum = (ProtocolEnum) protocol;
            return ProtocolDefinition.builder()
                    .type(protocolEnum.getCode())
                    .version(protocolEnum.getVersion())
                    .build();
        }
        
        return ProtocolDefinition.builder()
                .type(protocol.type())
                .version(protocol.getVersion())
                .build();
    }
    
    /**
     * 从ProtocolType枚举创建ProtocolDefinition
     */
    public static ProtocolDefinition fromProtocolType(com.muxin.gateway.core.plus.route.ProtocolType protocolType) {
        String version = getDefaultVersion(protocolType);
        return ProtocolDefinition.builder()
                .type(protocolType.getCode())
                .version(version)
                .build();
    }
    
    /**
     * 获取协议类型的默认版本
     */
    private static String getDefaultVersion(com.muxin.gateway.core.plus.route.ProtocolType protocolType) {
        switch (protocolType) {
            case HTTP:
                return "1.1";
            case GRPC:
                return "1.0";
            case TCP:
                return "1.0";
            case WEBSOCKET:
                return "13";
            default:
                return "1.0";
        }
    }
    
    /**
     * 检查是否需要协议转换
     */
    public boolean needsConversion(ProtocolDefinition outboundProtocol) {
        if (outboundProtocol == null) {
            return false;
        }
        
        return !this.type.equalsIgnoreCase(outboundProtocol.getType()) ||
               !this.version.equals(outboundProtocol.getVersion());
    }
    
    /**
     * 是否为HTTP协议
     */
    public boolean isHttp() {
        return "HTTP".equalsIgnoreCase(type);
    }
    
    /**
     * 是否为HTTP/2.0
     */
    public boolean isHttp2() {
        return isHttp() && "2.0".equals(version);
    }
    
    /**
     * 是否为gRPC协议
     */
    public boolean isGrpc() {
        return "GRPC".equalsIgnoreCase(type);
    }
    
    /**
     * 是否为TCP协议
     */
    public boolean isTcp() {
        return "TCP".equalsIgnoreCase(type);
    }
    
    /**
     * 是否为WebSocket协议
     */
    public boolean isWebSocket() {
        return "WEBSOCKET".equalsIgnoreCase(type);
    }
    
    /**
     * 获取协议的显示名称
     */
    public String getDisplayName() {
        try {
            ProtocolEnum protocolEnum = toProtocol();
            return protocolEnum.getDisplayName();
        } catch (Exception e) {
            return type + "/" + version;
        }
    }
}