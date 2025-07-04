package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.predicate.Predicate;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.filter.Filter;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 路由配置类 - 协议特定化设计
 * 每个路由专门处理一种协议，避免协议冲突
 * 
 * 设计原则：
 * 1. 一个Route只处理一种协议类型
 * 2. 路由的supportedProtocol与target的targetProtocol必须一致
 * 3. 简化配置，提高性能，避免协议转换开销
 *
 * @author muxin
 */
@Data
@Builder
public class RouteConfig {
    
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
     * 支持的协议（单一协议）
     * 每个路由专门处理一种协议，确保协议一致性
     */
    private Protocol supportedProtocol;
    
    /**
     * 断言列表（AND关系）
     */
    private List<Predicate> predicates;
    
    /**
     * 过滤器列表
     */
    private List<Filter> filters;
    
    /**
     * 目标服务配置
     */
    private RouteTarget target;
    
    /**
     * 路由元数据
     */
    private Map<String, Object> metadata;
    
    // ========== 超时配置 ==========
    
    /**
     * 连接超时时间
     */
    private Duration connectionTimeout;
    
    /**
     * 请求超时时间
     */
    private Duration requestTimeout;
    
    /**
     * 总超时时间（包含重试）
     */
    private Duration totalTimeout;
    
    /**
     * 读取超时时间
     */
    private Duration readTimeout;
    
    /**
     * 写入超时时间
     */
    private Duration writeTimeout;
    
    /**
     * 熔断器超时时间
     */
    private Duration circuitBreakerTimeout;
    
    /**
     * 是否启用超时控制
     */
    @Builder.Default
    private boolean timeoutEnabled = true;
    
    /**
     * 验证路由配置的一致性
     * 确保路由支持的协议与目标协议一致
     */
    public boolean isConfigurationValid() {
        if (supportedProtocol == null || target == null || target.getTargetProtocol() == null) {
            return false;
        }
        
        // 协议类型必须一致
        return supportedProtocol.getType().equals(target.getTargetProtocol().getType());
    }
    
    /**
     * 获取协议类型
     */
    public String getProtocolType() {
        return supportedProtocol != null ? supportedProtocol.getType().name() : "UNKNOWN";
    }
    
    /**
     * 获取指定类型的超时时间
     */
    public Duration getTimeout(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> connectionTimeout;
            case REQUEST -> requestTimeout;
            case TOTAL -> totalTimeout;
            case READ -> readTimeout;
            case WRITE -> writeTimeout;
            case CIRCUIT_BREAKER -> circuitBreakerTimeout;
        };
    }
    
    /**
     * 设置指定类型的超时时间
     */
    public void setTimeout(TimeoutType type, Duration timeout) {
        switch (type) {
            case CONNECTION -> this.connectionTimeout = timeout;
            case REQUEST -> this.requestTimeout = timeout;
            case TOTAL -> this.totalTimeout = timeout;
            case READ -> this.readTimeout = timeout;
            case WRITE -> this.writeTimeout = timeout;
            case CIRCUIT_BREAKER -> this.circuitBreakerTimeout = timeout;
        }
    }
    
    /**
     * 检查是否设置了指定类型的超时时间
     */
    public boolean hasTimeout(TimeoutType type) {
        return getTimeout(type) != null;
    }
    
    /**
     * 获取超时时间，如果未设置则返回默认值
     */
    public Duration getTimeoutOrDefault(TimeoutType type, Duration defaultValue) {
        Duration timeout = getTimeout(type);
        return timeout != null ? timeout : defaultValue;
    }
} 