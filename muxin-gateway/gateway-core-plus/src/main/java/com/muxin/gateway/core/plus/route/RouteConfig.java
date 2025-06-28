package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.predicate.UniversalPredicate;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.filter.UniversalFilter;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 路由配置类
 * 包含路由的所有配置信息，包括超时配置
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
     * 支持的协议列表
     */
    private List<Protocol> supportedProtocols;
    
    /**
     * 断言列表（AND关系）
     */
    private List<UniversalPredicate> predicates;
    
    /**
     * 过滤器列表
     */
    private List<UniversalFilter> filters;
    
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