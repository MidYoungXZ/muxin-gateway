package com.muxin.gateway.refactory.node;

import com.muxin.gateway.refactory.filter.FilterType;
import com.muxin.gateway.refactory.filter.HttpAuthFilter;
import com.muxin.gateway.refactory.filter.HttpLoggingFilter;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.predicate.HttpMethodPredicate;
import com.muxin.gateway.refactory.predicate.HttpPathPredicate;
import com.muxin.gateway.refactory.predicate.UniversalPredicate;
import com.muxin.gateway.refactory.route.RouteTarget;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;

import java.time.Duration;
import java.util.*;

/**
 * 增强的HTTP路由实现，支持过滤器和负载均衡
 *
 * @author muxin
 */
public class EnhancedHttpRoute implements UniversalRoute {
    
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final boolean enabled;
    private final List<Protocol> supportedProtocols;
    private final List<UniversalPredicate> predicates;
    private final List<UniversalFilter> filters;
    private final RouteTarget target;
    private final Map<String, Object> metadata;
    
    public EnhancedHttpRoute(String id, String name, String pathPattern, 
                           List<String> targetUris, LoadBalanceStrategy loadBalanceStrategy) {
        this.id = id;
        this.name = name;
        this.description = "Enhanced HTTP route for " + pathPattern;
        this.order = calculatePriority(pathPattern);
        this.enabled = true;
        
        // 支持HTTP协议
        this.supportedProtocols = Arrays.asList(new Protocol.HttpProtocol());
        
        // 创建断言
        this.predicates = createPredicates(pathPattern);
        
        // 创建过滤器链
        this.filters = createFilters();
        
        // 创建增强的路由目标
        this.target = new EnhancedRouteTarget(targetUris, loadBalanceStrategy);
        
        // 元数据
        this.metadata = new HashMap<>();
        metadata.put("pathPattern", pathPattern);
        metadata.put("targetUris", targetUris);
        metadata.put("loadBalanceStrategy", loadBalanceStrategy.getName());
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
    public List<Protocol> getSupportedProtocols() {
        return new ArrayList<>(supportedProtocols);
    }
    
    @Override
    public List<UniversalPredicate> getPredicates() {
        return new ArrayList<>(predicates);
    }
    
    @Override
    public List<UniversalFilter> getFilters() {
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
    public boolean matches(UniversalRequestContext context) {
        if (!enabled || context == null) {
            return false;
        }
        
        // 检查协议是否支持
        Protocol inboundProtocol = context.getInboundProtocol();
        if (inboundProtocol == null || !supportedProtocols.contains(inboundProtocol)) {
            return false;
        }
        
        // 执行所有断言（AND关系）
        for (UniversalPredicate predicate : predicates) {
            if (!predicate.test(context)) {
                return false;
            }
        }
        
        return true;
    }
    
    private List<UniversalPredicate> createPredicates(String pathPattern) {
        List<UniversalPredicate> predicateList = new ArrayList<>();
        
        // 添加路径断言
        predicateList.add(new HttpPathPredicate(pathPattern));
        
        // 可以根据需要添加更多断言
        // 例如：只允许GET和POST方法
        if (pathPattern.startsWith("/api/")) {
            predicateList.add(new HttpMethodPredicate("GET", "POST", "PUT", "DELETE"));
        }
        
        return predicateList;
    }
    
    private List<UniversalFilter> createFilters() {
        List<UniversalFilter> filterList = new ArrayList<>();
        
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