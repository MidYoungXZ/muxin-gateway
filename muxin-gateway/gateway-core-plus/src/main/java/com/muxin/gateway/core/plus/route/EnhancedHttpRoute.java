package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.filter.FilterType;
import com.muxin.gateway.core.plus.filter.HttpAuthFilter;
import com.muxin.gateway.core.plus.filter.HttpLoggingFilter;
import com.muxin.gateway.core.plus.filter.Filter;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.node.EnhancedRouteTarget;
import com.muxin.gateway.core.plus.predicate.HttpMethodPredicate;
import com.muxin.gateway.core.plus.predicate.HttpPathPredicate;
import com.muxin.gateway.core.plus.predicate.Predicate;
import com.muxin.gateway.core.plus.loadbalance.LoadBalanceStrategy;

import java.time.Duration;
import java.util.*;

/**
 * 增强的HTTP路由实现，支持过滤器和负载均衡
 * 专门处理HTTP协议，确保协议一致性
 *
 * @author muxin
 */
public class EnhancedHttpRoute implements Route {
    
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final boolean enabled;
    private final Protocol supportedProtocol;
    private final List<Predicate> predicates;
    private final List<Filter> filters;
    private final RouteTarget target;
    private final Map<String, Object> metadata;
    
    public EnhancedHttpRoute(String id, String name, String pathPattern, 
                           List<String> targetUris, LoadBalanceStrategy loadBalanceStrategy) {
        this.id = id;
        this.name = name;
        this.description = "Enhanced HTTP route for " + pathPattern;
        this.order = calculatePriority(pathPattern);
        this.enabled = true;
        
        // 支持单一HTTP协议
        this.supportedProtocol = new Protocol.HttpProtocol();
        
        // 创建断言
        this.predicates = createPredicates(pathPattern);
        
        // 创建过滤器链
        this.filters = createFilters();
        
        // 创建增强的路由目标
        this.target = new EnhancedRouteTarget(targetUris, loadBalanceStrategy);
        
        // 验证协议一致性
        if (!isConfigurationValid()) {
            throw new IllegalArgumentException(
                String.format("协议配置不一致: 路由协议=%s, 目标协议=%s", 
                    supportedProtocol.getType(), target.getTargetProtocol().getType()));
        }
        
        // 元数据
        this.metadata = new HashMap<>();
        metadata.put("pathPattern", pathPattern);
        metadata.put("targetUris", targetUris);
        metadata.put("loadBalanceStrategy", loadBalanceStrategy.getName());
        metadata.put("protocolType", getProtocolType());
        metadata.put("created", System.currentTimeMillis());
    }
    
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
        return supportedProtocol;
    }
    
    @Override
    public List<Predicate> getPredicates() {
        return new ArrayList<>(predicates);
    }
    
    @Override
    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }
    
    @Override
    public RouteTarget getTarget() {
        return target;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    @Override
    public boolean matches(RequestContext context) {
        if (!enabled || context == null) {
            return false;
        }
        
        // 检查协议是否匹配（单一协议匹配）
        Protocol inboundProtocol = context.getInboundProtocol();
        if (inboundProtocol == null || !supportedProtocol.getType().equals(inboundProtocol.getType())) {
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
    
    private List<Predicate> createPredicates(String pathPattern) {
        List<Predicate> predicateList = new ArrayList<>();
        
        // 添加路径断言
        predicateList.add(new HttpPathPredicate(pathPattern));
        
        // 可以根据需要添加更多断言
        // 例如：只允许GET和POST方法
        if (pathPattern.startsWith("/api/")) {
            predicateList.add(new HttpMethodPredicate("GET", "POST", "PUT", "DELETE"));
        }
        
        return predicateList;
    }
    
    private List<Filter> createFilters() {
        List<Filter> filterList = new ArrayList<>();
        
        // 添加日志过滤器（前置）
        filterList.add(new HttpLoggingFilter(FilterType.PRE, 100));
        
        // 添加认证过滤器（前置）
        if (name.contains("API")) {
            filterList.add(new HttpAuthFilter(200));
        }
        
        // 添加日志过滤器（后置）
        filterList.add(new HttpLoggingFilter(FilterType.POST, 900));
        
        return filterList;
    }
    
    private int calculatePriority(String pathPattern) {
        if ("/**".equals(pathPattern)) {
            return 9999; // 默认路由最低优先级
        }
        
        // 计算路径的具体程度
        int specificity = 0;
        
        // 统计非通配符的路径段
        String[] segments = pathPattern.split("/");
        for (String segment : segments) {
            if (!segment.isEmpty() && !segment.contains("*")) {
                specificity += 100; // 每个具体路径段增加100分
            } else if (segment.contains("*")) {
                specificity += 10; // 通配符段增加10分
            }
        }
        
        // 路径越长越具体
        specificity += pathPattern.length();
        
        // 转换为优先级（数值越小优先级越高）
        return 1000 - specificity;
    }
    
    // ========== 超时配置方法实现 ==========
    
    @Override
    public Duration getConnectionTimeout() {
        return Duration.ofSeconds(5); // HTTP连接超时5秒
    }
    
    @Override
    public Duration getRequestTimeout() {
        return Duration.ofSeconds(30); // HTTP请求超时30秒
    }
    
    @Override
    public Duration getTotalTimeout() {
        return Duration.ofSeconds(60); // HTTP总超时60秒
    }
    
    @Override
    public Duration getReadTimeout() {
        return Duration.ofSeconds(30); // HTTP读取超时30秒
    }
    
    @Override
    public Duration getWriteTimeout() {
        return Duration.ofSeconds(10); // HTTP写入超时10秒
    }
    
    @Override
    public Duration getCircuitBreakerTimeout() {
        return Duration.ofSeconds(60); // HTTP熔断器超时60秒
    }
} 