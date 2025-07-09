package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.predicate.Predicate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 增强的Route实现
 * 支持新的配置结构和单协议设计
 *
 * @author muxin
 */
@Data
@Builder
@Slf4j
public class EnhancedRoute implements Route {
    
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final boolean enabled;
    
    // 单协议配置
    private final Protocol inboundProtocol;
    private final List<Predicate> predicates;
    private final List<Filter> filters;
    private final EnhancedRouteTarget target;
    private final TimeoutConfig timeouts;
    private final Map<String, Object> metadata;
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public Protocol getSupportedProtocol() {
        // 单协议支持
        return inboundProtocol;
    }
    
    @Override
    public List<Predicate> getPredicates() {
        return predicates;
    }
    
    @Override
    public List<Filter> getFilters() {
        return filters;
    }
    
    @Override
    public RouteTarget getTarget() {
        return target;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean matches(RequestContext context) {
        if (!enabled || context == null) {
            return false;
        }
        
        // 检查入站协议是否匹配
        Protocol contextProtocol = context.getInboundProtocol();
        if (contextProtocol == null || !inboundProtocol.equals(contextProtocol)) {
            return false;
        }
        
        // 执行所有断言（AND关系）
        for (Predicate predicate : predicates) {
            if (!predicate.test(context)) {
                return false;
            }
        }
        
        return true;
    }
    
    // ========== 超时配置方法 ==========
    
    @Override
    public Duration getConnectionTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.CONNECTION)) {
            return timeouts.getTimeout(TimeoutType.CONNECTION);
        }
        return Duration.ofSeconds(5); // 默认连接超时5秒
    }
    
    @Override
    public Duration getRequestTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.REQUEST)) {
            return timeouts.getTimeout(TimeoutType.REQUEST);
        }
        return Duration.ofSeconds(30); // 默认请求超时30秒
    }
    
    @Override
    public Duration getTotalTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.TOTAL)) {
            return timeouts.getTimeout(TimeoutType.TOTAL);
        }
        return Duration.ofSeconds(60); // 默认总超时60秒
    }
    
    @Override
    public Duration getReadTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.READ)) {
            return timeouts.getTimeout(TimeoutType.READ);
        }
        return Duration.ofSeconds(30); // 默认读取超时30秒
    }
    
    @Override
    public Duration getWriteTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.WRITE)) {
            return timeouts.getTimeout(TimeoutType.WRITE);
        }
        return Duration.ofSeconds(10); // 默认写入超时10秒
    }
    
    @Override
    public Duration getCircuitBreakerTimeout() {
        if (timeouts != null && timeouts.hasTimeout(TimeoutType.CIRCUIT_BREAKER)) {
            return timeouts.getTimeout(TimeoutType.CIRCUIT_BREAKER);
        }
        return Duration.ofSeconds(60); // 默认熔断器超时60秒
    }
    
    // ========== 扩展方法 ==========
    
    /**
     * 获取入站协议
     */
    public Protocol getInboundProtocol() {
        return inboundProtocol;
    }
    
    /**
     * 获取出站协议
     */
    public Protocol getOutboundProtocol() {
        return target.getTargetProtocol();
    }
    
    /**
     * 检查是否需要协议转换
     */
    public boolean needsProtocolConversion() {
        Protocol outbound = getOutboundProtocol();
        return outbound != null && !inboundProtocol.equals(outbound);
    }
    
    /**
     * 获取协议转换类型
     */
    public String getProtocolConversionType() {
        if (!needsProtocolConversion()) {
            return "NONE";
        }
        
        return inboundProtocol.getType().name() + "_TO_" + 
               getOutboundProtocol().getType().name();
    }
    
    /**
     * 获取服务名称
     */
    public String getServiceName() {
        // 优先从目标配置获取
        if (target instanceof EnhancedRouteTarget) {
            EnhancedRouteTarget enhancedTarget = (EnhancedRouteTarget) target;
            if (enhancedTarget.isDiscovery()) {
                return enhancedTarget.getServiceName();
            }
        }
        
        // 从元数据获取
        if (metadata != null) {
            Object serviceName = metadata.get("service-name");
            if (serviceName != null) {
                return serviceName.toString();
            }
        }
        
        // 默认使用路由ID
        return id;
    }
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        if (target instanceof EnhancedRouteTarget) {
            EnhancedRouteTarget enhancedTarget = (EnhancedRouteTarget) target;
            return enhancedTarget.getLoadBalanceStrategy();
        }
        return "ROUND_ROBIN";
    }
    
    /**
     * 检查是否为静态目标
     */
    public boolean isStaticTarget() {
        if (target instanceof EnhancedRouteTarget) {
            EnhancedRouteTarget enhancedTarget = (EnhancedRouteTarget) target;
            return enhancedTarget.isStatic();
        }
        return false;
    }
    
    /**
     * 检查是否为服务发现目标
     */
    public boolean isDiscoveryTarget() {
        if (target instanceof EnhancedRouteTarget) {
            EnhancedRouteTarget enhancedTarget = (EnhancedRouteTarget) target;
            return enhancedTarget.isDiscovery();
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("EnhancedRoute{id='%s', name='%s', order=%d, enabled=%s, " +
                           "inbound=%s, outbound=%s, predicates=%d, filters=%d}", 
                           id, name, order, enabled, 
                           inboundProtocol.getType(), 
                           getOutboundProtocol() != null ? getOutboundProtocol().getType() : "null",
                           predicates != null ? predicates.size() : 0,
                           filters != null ? filters.size() : 0);
    }
} 