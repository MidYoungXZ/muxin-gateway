package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.predicate.Predicate;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.filter.Filter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 通用路由接口 - 支持多协议
 *
 * @author muxin
 */
public interface Route {
    
    /**
     * 路由ID
     */
    String getId();
    
    /**
     * 路由名称
     */
    String getName();
    
    /**
     * 路由描述
     */
    String getDescription();
    
    /**
     * 路由优先级（数值越小优先级越高）
     */
    int getOrder();
    
    /**
     * 是否启用
     */
    boolean isEnabled();
    
    /**
     * 支持的协议列表
     */
    List<Protocol> getSupportedProtocols();
    
    /**
     * 断言列表（AND关系）
     */
    List<Predicate> getPredicates();
    
    /**
     * 过滤器列表
     */
    List<Filter> getFilters();
    
    /**
     * 目标服务配置
     */
    RouteTarget getTarget();
    
    /**
     * 路由元数据
     */
    Map<String, Object> getMetadata();
    
    /**
     * 匹配请求上下文
     */
    boolean matches(RequestContext context);
    
    // ========== 超时配置方法 ==========
    
    /**
     * 获取连接超时时间
     */
    Duration getConnectionTimeout();
    
    /**
     * 获取请求超时时间
     */
    Duration getRequestTimeout();
    
    /**
     * 获取总超时时间（包含重试）
     */
    Duration getTotalTimeout();
    
    /**
     * 获取读取超时时间
     */
    Duration getReadTimeout();
    
    /**
     * 获取写入超时时间
     */
    Duration getWriteTimeout();
    
    /**
     * 获取熔断器超时时间
     */
    Duration getCircuitBreakerTimeout();
    
    /**
     * 是否启用超时控制
     */
    default boolean isTimeoutEnabled() {
        return true;
    }
    
    /**
     * 获取指定类型的超时时间
     */
    default Duration getTimeout(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> getConnectionTimeout();
            case REQUEST -> getRequestTimeout();
            case TOTAL -> getTotalTimeout();
            case READ -> getReadTimeout();
            case WRITE -> getWriteTimeout();
            case CIRCUIT_BREAKER -> getCircuitBreakerTimeout();
        };
    }
} 