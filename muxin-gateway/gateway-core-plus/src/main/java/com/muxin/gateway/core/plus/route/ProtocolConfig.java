package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.ProtocolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议配置类
 * 支持单协议配置
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolConfig {
    
    /**
     * 协议类型
     */
    private String type;
    
    /**
     * 协议版本
     */
    @Builder.Default
    private String version = "1.1";
    
    /**
     * 转换为Protocol对象
     */
    public Protocol toProtocol() {
        ProtocolType protocolType = ProtocolType.valueOf(type.toUpperCase());
        
        return switch (protocolType) {
            case HTTP -> new Protocol.HttpProtocol(version);
            default -> throw new IllegalArgumentException("不支持的协议类型: " + type);
        };
    }
    
    /**
     * 从Protocol对象创建ProtocolConfig
     */
    public static ProtocolConfig fromProtocol(Protocol protocol) {
        return ProtocolConfig.builder()
                .type(protocol.getType().name())
                .version(protocol.getVersion())
                .build();
    }
    
    /**
     * 检查是否需要协议转换
     */
    public boolean needsConversion(ProtocolConfig outboundProtocol) {
        if (outboundProtocol == null) {
            return false;
        }
        
        return !this.type.equalsIgnoreCase(outboundProtocol.getType()) ||
               !this.version.equals(outboundProtocol.getVersion());
    }
} 