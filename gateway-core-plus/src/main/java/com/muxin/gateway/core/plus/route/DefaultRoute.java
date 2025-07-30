package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.predicate.Predicate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认路由实现类
 * 提供Route接口的完整实现
 *
 * @author muxin
 */
@Data
@Builder
@Slf4j
public class DefaultRoute implements Route {
    
    /**
     * 路由ID
     */
    private final String id;
    
    /**
     * 路由名称
     */
    private final String name;
    
    /**
     * 路由描述
     */
    private final String description;
    
    /**
     * 路由优先级（数值越小优先级越高）
     */
    private final int order;
    
    /**
     * 是否启用
     */
    private final boolean enabled;
    
    /**
     * 支持的协议
     */
    private final Protocol supportedProtocol;
    
    /**
     * 断言列表
     */
    private final List<Predicate> predicates;
    
    /**
     * 过滤器列表
     */
    private final List<Filter> filters;
    
    /**
     * 目标服务配置
     */
    private final RouteService service;
    
    /**
     * 负载均衡策略
     */
    private final LoadBalanceStrategy loadBalanceStrategy;
    
    /**
     * 路由元数据
     */
    private final Map<String, Object> metadata;
    
    /**
     * 超时配置
     */
    private final TimeoutConfig timeoutConfig;
    
    /**
     * 构造函数中的验证
     */
    public DefaultRoute(String id, String name, String description, int order, boolean enabled,
                       Protocol supportedProtocol, List<Predicate> predicates, List<Filter> filters,
                       RouteService service, LoadBalanceStrategy loadBalanceStrategy,
                       Map<String, Object> metadata, TimeoutConfig timeoutConfig) {
        this.id = Objects.requireNonNull(id, "路由ID不能为空");
        this.name = Objects.requireNonNull(name, "路由名称不能为空");
        this.description = description;
        this.order = order;
        this.enabled = enabled;
        this.supportedProtocol = Objects.requireNonNull(supportedProtocol, "支持协议不能为空");
        this.predicates = Objects.requireNonNull(predicates, "断言列表不能为空");
        this.filters = Objects.requireNonNull(filters, "过滤器列表不能为空");
        this.service = Objects.requireNonNull(service, "目标服务不能为空");
        this.loadBalanceStrategy = Objects.requireNonNull(loadBalanceStrategy, "负载均衡策略不能为空");
        this.metadata = metadata != null ? metadata : java.util.Collections.emptyMap();
        this.timeoutConfig = timeoutConfig;
        
        log.debug("创建路由: {} (协议: {}, 策略: {})", 
                id, supportedProtocol.type(), loadBalanceStrategy.getStrategyName());
    }
    
    @Override
    public boolean matches(RequestContext context) {
        if (!enabled) {
            log.debug("路由已禁用，跳过匹配: {}", id);
            return false;
        }
        
        if (context == null) {
            log.warn("请求上下文为空，路由匹配失败: {}", id);
            return false;
        }
        
        // 所有断言都必须匹配（AND关系）
        for (Predicate predicate : predicates) {
            if (!predicate.test(context.exchange())) {
                log.debug("断言匹配失败: {} - {}", predicate.getName(), id);
                return false;
            }
        }
        
        log.debug("路由匹配成功: {}", id);
        return true;
    }
    
    // ========== 超时配置方法实现 ==========
    
    @Override
    public Duration getConnectionTimeout() {
        return timeoutConfig != null ? timeoutConfig.getConnection() : Duration.ofSeconds(5);
    }
    
    @Override
    public Duration getRequestTimeout() {
        return timeoutConfig != null ? timeoutConfig.getRequest() : Duration.ofSeconds(30);
    }
    
    @Override
    public Duration getTotalTimeout() {
        return timeoutConfig != null ? timeoutConfig.getTotal() : Duration.ofSeconds(60);
    }
    
    @Override
    public Duration getReadTimeout() {
        return timeoutConfig != null ? timeoutConfig.getRead() : Duration.ofSeconds(30);
    }
    
    @Override
    public Duration getWriteTimeout() {
        return timeoutConfig != null ? timeoutConfig.getWrite() : Duration.ofSeconds(30);
    }
    
    @Override
    public Duration getCircuitBreakerTimeout() {
        return timeoutConfig != null ? timeoutConfig.getCircuitBreaker() : Duration.ofSeconds(10);
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 获取路由统计信息
     */
    public String getRouteStats() {
        return String.format("Route{id='%s', enabled=%s, predicates=%d, filters=%d, strategy='%s'}", 
                id, enabled, predicates.size(), filters.size(), loadBalanceStrategy.getStrategyName());
    }
    
    /**
     * 验证路由的完整性
     */
    public boolean isValid() {
        try {
            // 基础字段验证
            if (id == null || id.trim().isEmpty()) {
                return false;
            }
            
            if (supportedProtocol == null || service == null || loadBalanceStrategy == null) {
                return false;
            }
            
            // 协议一致性验证
            if (!isConfigurationValid()) {
                log.warn("路由协议配置不一致: {} - 路由协议: {}, 服务协议: {}", 
                        id, supportedProtocol.type(), service.supportProtocol().type());
                return false;
            }
            
            // 断言和过滤器验证
            if (predicates == null || predicates.isEmpty()) {
                log.warn("路由断言列表为空: {}", id);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("路由验证失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DefaultRoute that = (DefaultRoute) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("DefaultRoute{id='%s', name='%s', enabled=%s, order=%d, protocol='%s', strategy='%s'}", 
                id, name, enabled, order, 
                supportedProtocol != null ? supportedProtocol.type() : "null",
                loadBalanceStrategy != null ? loadBalanceStrategy.getStrategyName() : "null");
    }
} 