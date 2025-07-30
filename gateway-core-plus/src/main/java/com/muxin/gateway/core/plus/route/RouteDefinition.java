package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.ProtocolDefinition;
import com.muxin.gateway.core.plus.route.filter.FilterDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.plus.route.predicate.PredicateDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 增强的路由配置类
 * 支持新的YAML配置结构，负载均衡配置移到路由级别
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDefinition {
    
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
    private ProtocolDefinition supportProtocol;
    
    /**
     * 断言配置列表（AND关系）
     */
    private List<PredicateDefinition> predicates;
    
    /**
     * 过滤器配置列表
     */
    private List<FilterDefinition> filters;
    
    /**
     * 目标服务配置
     */
    private ServiceDefinition service;
    
    /**
     * 负载均衡配置（路由级别）
     */
    private LoadBalanceDefinition loadBalance;
    
    /**
     * 超时配置
     */
    private TimeoutConfig timeouts;
    
    /**
     * 路由元数据
     */
    private Map<String, Object> metadata;
    
    // ========== 负载均衡配置方法 ==========
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        return loadBalance != null ? loadBalance.getStrategy() : "ROUND_ROBIN";
    }
    
    /**
     * 是否配置了自定义负载均衡策略
     */
    public boolean hasCustomLoadBalance() {
        return loadBalance != null;
    }
    
    /**
     * 获取有效的负载均衡配置（考虑默认值）
     */
    public LoadBalanceDefinition getEffectiveLoadBalance() {
        if (loadBalance != null) {
            return loadBalance;
        }
        
        // 返回默认的负载均衡配置
        return LoadBalanceDefinition.builder()
            .strategy("ROUND_ROBIN")
            .build();
    }
    
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
        
        if (supportProtocol == null) {
            throw new IllegalArgumentException("入站协议不能为空");
        }
        
        if (predicates == null || predicates.isEmpty()) {
            throw new IllegalArgumentException("断言配置不能为空");
        }
        
        if (service == null) {
            throw new IllegalArgumentException("服务配置不能为空");
        }
        
        // 验证服务配置的基本信息
        if (service.getType() == null) {
            throw new IllegalArgumentException("服务类型不能为空");
        }

        // CONFIG类型需要验证addresses
        if (service.isConfigType() && (service.getAddresses() == null || service.getAddresses().isEmpty())) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置addresses");
        }
        
        // 验证负载均衡配置
        validateLoadBalanceConfig();
        
        // 验证协议转换
        if (supportProtocol.needsConversion(service.getSupportProtocol())) {
            validateProtocolConversion();
        }
    }
    
    /**
     * 验证负载均衡配置
     */
    private void validateLoadBalanceConfig() {
        if (loadBalance != null) {
            // 验证策略名称不能为空
            if (loadBalance.getStrategy() == null || loadBalance.getStrategy().trim().isEmpty()) {
                throw new IllegalArgumentException("负载均衡策略名称不能为空");
            }
            
            // 验证策略名称是否有效
            String strategy = loadBalance.getStrategy().toUpperCase();
            if (!isValidLoadBalanceStrategy(strategy)) {
                throw new IllegalArgumentException("不支持的负载均衡策略: " + loadBalance.getStrategy());
            }
        }
    }
    
    /**
     * 检查负载均衡策略是否有效
     */
    private boolean isValidLoadBalanceStrategy(String strategy) {
        return "ROUND_ROBIN".equals(strategy) ||
               "RANDOM".equals(strategy) ||
               "WEIGHTED_ROUND_ROBIN".equals(strategy) ||
               "LEAST_CONNECTIONS".equals(strategy) ||
               "CONSISTENT_HASH".equals(strategy);
    }
    
    /**
     * 验证协议转换
     */
    private void validateProtocolConversion() {
        String inboundType = supportProtocol.getType();
        String outboundType = service.getSupportProtocol().getType();
        
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
        return supportProtocol.needsConversion(service.getSupportProtocol());
    }
    
    /**
     * 获取协议转换类型
     */
    public String getProtocolConversionType() {
        if (!needsProtocolConversion()) {
            return "NONE";
        }
        
        return supportProtocol.getType().toUpperCase() + "_TO_" +
               service.getSupportProtocol().getType().toUpperCase();
    }
    
    /**
     * 获取服务名称
     */
    public String getServiceName() {
        if (service.isDiscoveryType()) {
            return service.getName();
        }
        
        // 从元数据获取服务名称
        if (metadata != null) {
            Object serviceName = metadata.get("service-name");
            if (serviceName != null) {
                return serviceName.toString();
            }
        }
        
        // 默认使用服务名称
        return service.getName();
    }
} 