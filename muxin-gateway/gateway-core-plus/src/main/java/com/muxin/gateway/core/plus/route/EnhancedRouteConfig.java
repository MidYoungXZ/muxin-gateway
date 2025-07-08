package com.muxin.gateway.core.plus.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 增强的路由配置类
 * 支持新的YAML配置结构
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedRouteConfig {
    
    /**
     * 路由ID
     */
    private String id;
    
    /**
     * 路由名称
     */
    private String name;
    
    /**
     * 路由描述
     */
    private String description;
    
    /**
     * 路由优先级（数值越小优先级越高）
     */
    @Builder.Default
    private int order = 0;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;
    
    /**
     * 入站协议配置（单协议）
     */
    private ProtocolConfig inboundProtocol;
    
    /**
     * 断言配置列表（AND关系）
     */
    private List<PredicateConfig> predicates;
    
    /**
     * 过滤器配置列表
     */
    private List<FilterConfig> filters;
    
    /**
     * 目标服务配置
     */
    private EnhancedRouteTarget target;
    
    /**
     * 超时配置
     */
    private TimeoutConfig timeouts;
    
    /**
     * 路由元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 验证配置
     */
    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("路由名称不能为空");
        }
        
        if (inboundProtocol == null) {
            throw new IllegalArgumentException("入站协议不能为空");
        }
        
        if (predicates == null || predicates.isEmpty()) {
            throw new IllegalArgumentException("断言配置不能为空");
        }
        
        if (target == null) {
            throw new IllegalArgumentException("目标配置不能为空");
        }
        
        // 验证目标配置
        target.validate();
        
        // 验证协议转换
        if (inboundProtocol.needsConversion(target.getOutboundProtocol())) {
            validateProtocolConversion();
        }
    }
    
    /**
     * 验证协议转换
     */
    private void validateProtocolConversion() {
        String inboundType = inboundProtocol.getType();
        String outboundType = target.getOutboundProtocol().getType();
        
        // 检查是否支持协议转换
        if (!isSupportedProtocolConversion(inboundType, outboundType)) {
            throw new IllegalArgumentException("不支持的协议转换: " + inboundType + " -> " + outboundType);
        }
    }
    
    /**
     * 检查是否支持协议转换
     */
    private boolean isSupportedProtocolConversion(String inbound, String outbound) {
        // 相同协议总是支持
        if (inbound.equalsIgnoreCase(outbound)) {
            return true;
        }
        
        // HTTP可以转换为大部分协议
        if ("HTTP".equalsIgnoreCase(inbound)) {
            return "GRPC".equalsIgnoreCase(outbound) || 
                   "TCP".equalsIgnoreCase(outbound) ||
                   "WEBSOCKET".equalsIgnoreCase(outbound);
        }
        
        // WebSocket可以转换为TCP
        if ("WEBSOCKET".equalsIgnoreCase(inbound)) {
            return "TCP".equalsIgnoreCase(outbound);
        }
        
        // 其他协议转换待实现
        return false;
    }
    
    /**
     * 检查是否需要协议转换
     */
    public boolean needsProtocolConversion() {
        return inboundProtocol.needsConversion(target.getOutboundProtocol());
    }
    
    /**
     * 获取协议转换类型
     */
    public String getProtocolConversionType() {
        if (!needsProtocolConversion()) {
            return "NONE";
        }
        
        return inboundProtocol.getType().toUpperCase() + "_TO_" + 
               target.getOutboundProtocol().getType().toUpperCase();
    }
    
    /**
     * 获取服务名称
     */
    public String getServiceName() {
        if (target.isDiscovery()) {
            return target.getServiceName();
        }
        
        // 从元数据获取服务名称
        if (metadata != null) {
            Object serviceName = metadata.get("service-name");
            if (serviceName != null) {
                return serviceName.toString();
            }
        }
        
        // 默认使用路由ID作为服务名称
        return id;
    }
} 