package com.muxin.gateway.refactory.route;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.predicate.UniversalPredicate;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UniversalRoute 构建器
 * 提供便捷的链式API来创建路由，包括超时配置
 *
 * @author muxin
 */
@Slf4j
public class UniversalRouteBuilder {
    
    private String id;
    private String name;
    private String description;
    private int order = 0;
    private boolean enabled = true;
    private List<Protocol> supportedProtocols = new ArrayList<>();
    private List<UniversalPredicate> predicates = new ArrayList<>();
    private List<UniversalFilter> filters = new ArrayList<>();
    private RouteTarget target;
    private Map<String, Object> metadata = new HashMap<>();
    
    // 超时配置字段
    private Duration connectionTimeout;
    private Duration requestTimeout;
    private Duration totalTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration circuitBreakerTimeout;
    private boolean timeoutEnabled = true;
    
    private UniversalRouteBuilder() {
        // 私有构造函数，通过静态方法创建
    }
    
    /**
     * 创建新的构建器
     */
    public static UniversalRouteBuilder create() {
        return new UniversalRouteBuilder();
    }
    
    /**
     * 从现有路由创建构建器（复制配置）
     */
    public static UniversalRouteBuilder from(UniversalRoute route) {
        return new UniversalRouteBuilder()
                .id(route.getId())
                .name(route.getName())
                .description(route.getDescription())
                .order(route.getOrder())
                .enabled(route.isEnabled())
                .supportedProtocols(route.getSupportedProtocols())
                .predicates(route.getPredicates())
                .filters(route.getFilters())
                .target(route.getTarget())
                .metadata(route.getMetadata())
                .connectionTimeout(route.getConnectionTimeout())
                .requestTimeout(route.getRequestTimeout())
                .totalTimeout(route.getTotalTimeout())
                .readTimeout(route.getReadTimeout())
                .writeTimeout(route.getWriteTimeout())
                .circuitBreakerTimeout(route.getCircuitBreakerTimeout())
                .timeoutEnabled(route.isTimeoutEnabled());
    }
    
    // ========== 基础配置方法 ==========
    
    public UniversalRouteBuilder id(String id) {
        this.id = id;
        return this;
    }
    
    public UniversalRouteBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public UniversalRouteBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public UniversalRouteBuilder order(int order) {
        this.order = order;
        return this;
    }
    
    public UniversalRouteBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public UniversalRouteBuilder supportedProtocols(List<Protocol> protocols) {
        if (protocols != null) {
            this.supportedProtocols = new ArrayList<>(protocols);
        }
        return this;
    }
    
    public UniversalRouteBuilder addProtocol(Protocol protocol) {
        if (protocol != null) {
            this.supportedProtocols.add(protocol);
        }
        return this;
    }
    
    public UniversalRouteBuilder predicates(List<UniversalPredicate> predicates) {
        if (predicates != null) {
            this.predicates = new ArrayList<>(predicates);
        }
        return this;
    }
    
    public UniversalRouteBuilder addPredicate(UniversalPredicate predicate) {
        if (predicate != null) {
            this.predicates.add(predicate);
        }
        return this;
    }
    
    public UniversalRouteBuilder filters(List<UniversalFilter> filters) {
        if (filters != null) {
            this.filters = new ArrayList<>(filters);
        }
        return this;
    }
    
    public UniversalRouteBuilder addFilter(UniversalFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return this;
    }
    
    public UniversalRouteBuilder target(RouteTarget target) {
        this.target = target;
        return this;
    }
    
    public UniversalRouteBuilder metadata(Map<String, Object> metadata) {
        if (metadata != null) {
            this.metadata = new HashMap<>(metadata);
        }
        return this;
    }
    
    public UniversalRouteBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    // ========== 超时配置方法 ==========
    
    public UniversalRouteBuilder connectionTimeout(Duration timeout) {
        this.connectionTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder connectionTimeout(long seconds) {
        this.connectionTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder requestTimeout(Duration timeout) {
        this.requestTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder requestTimeout(long seconds) {
        this.requestTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder totalTimeout(Duration timeout) {
        this.totalTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder totalTimeout(long seconds) {
        this.totalTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder readTimeout(Duration timeout) {
        this.readTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder readTimeout(long seconds) {
        this.readTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder writeTimeout(Duration timeout) {
        this.writeTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder writeTimeout(long seconds) {
        this.writeTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder circuitBreakerTimeout(Duration timeout) {
        this.circuitBreakerTimeout = timeout;
        return this;
    }
    
    public UniversalRouteBuilder circuitBreakerTimeout(long seconds) {
        this.circuitBreakerTimeout = Duration.ofSeconds(seconds);
        return this;
    }
    
    public UniversalRouteBuilder timeoutEnabled(boolean enabled) {
        this.timeoutEnabled = enabled;
        return this;
    }
    
    // ========== 便捷配置方法 ==========
    
    /**
     * 快速API配置（低延迟）
     */
    public UniversalRouteBuilder fastApi() {
        return connectionTimeout(Duration.ofSeconds(2))
               .requestTimeout(Duration.ofSeconds(5))
               .totalTimeout(Duration.ofSeconds(10))
               .readTimeout(Duration.ofSeconds(5))
               .writeTimeout(Duration.ofSeconds(3));
    }
    
    /**
     * 慢速API配置（高延迟）
     */
    public UniversalRouteBuilder slowApi() {
        return connectionTimeout(Duration.ofSeconds(10))
               .requestTimeout(Duration.ofSeconds(60))
               .totalTimeout(Duration.ofSeconds(120))
               .readTimeout(Duration.ofSeconds(60))
               .writeTimeout(Duration.ofSeconds(30));
    }
    
    /**
     * 计算密集型API配置
     */
    public UniversalRouteBuilder computeIntensiveApi() {
        return connectionTimeout(Duration.ofSeconds(5))
               .requestTimeout(Duration.ofMinutes(5))
               .totalTimeout(Duration.ofMinutes(10))
               .readTimeout(Duration.ofMinutes(5))
               .writeTimeout(Duration.ofSeconds(30));
    }
    
    /**
     * 文件上传API配置
     */
    public UniversalRouteBuilder fileUploadApi() {
        return connectionTimeout(Duration.ofSeconds(10))
               .requestTimeout(Duration.ofMinutes(10))
               .totalTimeout(Duration.ofMinutes(15))
               .readTimeout(Duration.ofMinutes(5))
               .writeTimeout(Duration.ofMinutes(10));
    }
    
    /**
     * 外部API调用配置
     */
    public UniversalRouteBuilder externalApi() {
        return connectionTimeout(Duration.ofSeconds(15))
               .requestTimeout(Duration.ofSeconds(30))
               .totalTimeout(Duration.ofSeconds(60))
               .readTimeout(Duration.ofSeconds(30))
               .writeTimeout(Duration.ofSeconds(15))
               .circuitBreakerTimeout(Duration.ofSeconds(120));
    }
    
    // ========== 验证方法 ==========
    
    /**
     * 验证配置是否有效
     */
    public UniversalRouteBuilder validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        
        if (target == null) {
            throw new IllegalArgumentException("路由目标不能为空");
        }
        
        // 验证超时配置的合理性
        validateTimeoutConfig();
        
        return this;
    }
    
    private void validateTimeoutConfig() {
        if (requestTimeout != null && totalTimeout != null && 
            requestTimeout.compareTo(totalTimeout) > 0) {
            log.warn("路由 {} 请求超时 {} 大于总超时 {}", id, requestTimeout, totalTimeout);
        }
        
        if (connectionTimeout != null && requestTimeout != null && 
            connectionTimeout.compareTo(requestTimeout) > 0) {
            log.warn("路由 {} 连接超时 {} 大于请求超时 {}", id, connectionTimeout, requestTimeout);
        }
    }
    
    // ========== 构建方法 ==========
    
    /**
     * 构建 UniversalRoute
     */
    public UniversalRoute build() {
        validate();
        
        // 从metadata中提取pathPattern和targetUris，如果没有则使用默认值
        String pathPattern = (String) metadata.getOrDefault("pathPattern", "/**");
        @SuppressWarnings("unchecked")
        List<String> targetUris = (List<String>) metadata.get("targetUris");
        
        // 如果没有配置targetUris，从target中提取
        if (targetUris == null && target != null && target.getTargetAddresses() != null) {
            targetUris = target.getTargetAddresses().stream()
                    .map(addr -> addr.toUri())
                    .toList();
        }
        
        // 如果还是没有，使用默认值
        if (targetUris == null || targetUris.isEmpty()) {
            targetUris = List.of("http://localhost:8080");
        }
        
        // 获取负载均衡策略
        com.muxin.gateway.refactory.loadbalance.LoadBalanceStrategy loadBalanceStrategy = 
            new com.muxin.gateway.refactory.loadbalance.RoundRobinLoadBalancer();
        
        // 创建 EnhancedHttpRoute
        EnhancedHttpRoute route = new EnhancedHttpRoute(id, name, pathPattern, targetUris, loadBalanceStrategy);
        
        log.debug("构建路由完成: {}", route);
        
        return route;
    }
    
    /**
     * 构建配置对象
     */
    public RouteConfig buildConfig() {
        validate();
        
        return RouteConfig.builder()
                .id(id)
                .name(name)
                .description(description)
                .order(order)
                .enabled(enabled)
                .supportedProtocols(supportedProtocols)
                .predicates(predicates)
                .filters(filters)
                .target(target)
                .metadata(metadata)
                .connectionTimeout(connectionTimeout)
                .requestTimeout(requestTimeout)
                .totalTimeout(totalTimeout)
                .readTimeout(readTimeout)
                .writeTimeout(writeTimeout)
                .circuitBreakerTimeout(circuitBreakerTimeout)
                .timeoutEnabled(timeoutEnabled)
                .build();
    }
} 