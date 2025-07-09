package com.muxin.gateway.core.plus.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 地址配置类
 * 支持静态地址和lb://协议
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDefinition {
    
    /**
     * 地址URI
     * 静态配置: http://host:port
     * 服务发现: lb://service-name
     */
    private String uri;
    
    /**
     * 权重（仅用于静态配置）
     */
    @Builder.Default
    private Integer weight = 100;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 检查是否为服务发现地址
     */
    public boolean isDiscoveryAddress() {
        return uri != null && uri.startsWith("lb://");
    }
    
    /**
     * 检查是否为静态地址
     */
    public boolean isStaticAddress() {
        return uri != null && (uri.startsWith("http://") || uri.startsWith("https://"));
    }
    
    /**
     * 获取服务名称（仅适用于lb://协议）
     */
    public String getServiceName() {
        if (!isDiscoveryAddress()) {
            throw new IllegalStateException("只有lb://协议才能获取服务名称");
        }
        return uri.substring(5); // 去掉 "lb://" 前缀
    }
    
    /**
     * 验证地址配置
     */
    public void validate() {
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("地址URI不能为空");
        }
        
        if (!isDiscoveryAddress() && !isStaticAddress()) {
            throw new IllegalArgumentException("不支持的地址格式: " + uri + "，仅支持 http://、https:// 或 lb:// 协议");
        }
        
        if (isDiscoveryAddress() && weight != null && weight != 100) {
            throw new IllegalArgumentException("服务发现地址不支持设置权重");
        }
    }
} 