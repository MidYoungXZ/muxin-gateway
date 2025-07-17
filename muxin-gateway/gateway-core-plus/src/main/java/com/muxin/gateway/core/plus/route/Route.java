package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.predicate.Predicate;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 通用路由接口 - 协议特定化设计
 * 每个路由专门处理一种协议，简化设计并避免协议冲突
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
     * 支持的协议（单一协议）
     * 每个路由专门处理一种协议，确保协议一致性
     */
    Protocol getSupportedProtocol();
    
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
    RouteService getService();
    
    /**
     * 负载均衡策略
     * 如果未配置则返回默认策略
     */
    LoadBalanceStrategy getLoadBalanceStrategy();
    
    /**
     * 路由元数据
     */
    Map<String, Object> getMetadata();
    
    /**
     * 匹配请求上下文
     */
    boolean matches(RequestContext context);
    
    /**
     * 验证路由配置的一致性
     * 确保路由支持的协议与目标协议一致
     */
    default boolean isConfigurationValid() {
        Protocol supportedProtocol = getSupportedProtocol();
        Protocol targetProtocol = getService().supportProtocol();
        
        if (supportedProtocol == null || targetProtocol == null) {
            return false;
        }
        
        // 协议类型必须一致
        return supportedProtocol.type().equals(targetProtocol.type());
    }
    
    /**
     * 获取协议类型
     * 便于快速获取路由处理的协议类型
     */
    default String getProtocolType() {
        Protocol protocol = getSupportedProtocol();
        return protocol != null ? protocol.type() : "UNKNOWN";
    }
    
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
        switch (type) {
            case CONNECTION:
                return getConnectionTimeout();
            case REQUEST:
                return getRequestTimeout();
            case TOTAL:
                return getTotalTimeout();
            case READ:
                return getReadTimeout();
            case WRITE:
                return getWriteTimeout();
            case CIRCUIT_BREAKER:
                return getCircuitBreakerTimeout();
            default:
                return null;
        }
    }
} 