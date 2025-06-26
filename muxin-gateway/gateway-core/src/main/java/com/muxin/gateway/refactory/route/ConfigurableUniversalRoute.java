package com.muxin.gateway.refactory.route;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.predicate.UniversalPredicate;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 配置驱动的通用路由实现
 * 使用 RouteConfig 来管理路由配置，包括超时配置
 *
 * @author muxin
 */
@Slf4j
public class ConfigurableUniversalRoute implements UniversalRoute {
    
    // 基础配置
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
    
    // 超时配置，支持默认值
    private final Duration connectionTimeout;
    private final Duration requestTimeout;
    private final Duration totalTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final Duration circuitBreakerTimeout;
    private final boolean timeoutEnabled;
    
    // 默认超时配置
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_TOTAL_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_CIRCUIT_BREAKER_TIMEOUT = Duration.ofSeconds(60);
    
    /**
     * 通过 RouteConfig 构造路由
     */
    public ConfigurableUniversalRoute(RouteConfig config) {
        this.id = config.getId();
        this.name = config.getName();
        this.description = config.getDescription();
        this.order = config.getOrder();
        this.enabled = config.isEnabled();
        this.supportedProtocols = config.getSupportedProtocols();
        this.predicates = config.getPredicates();
        this.filters = config.getFilters();
        this.target = config.getTarget();
        this.metadata = config.getMetadata();
        this.timeoutEnabled = config.isTimeoutEnabled();
        
        // 超时配置，如果未设置则使用默认值
        this.connectionTimeout = config.getTimeoutOrDefault(TimeoutType.CONNECTION, DEFAULT_CONNECTION_TIMEOUT);
        this.requestTimeout = config.getTimeoutOrDefault(TimeoutType.REQUEST, DEFAULT_REQUEST_TIMEOUT);
        this.totalTimeout = config.getTimeoutOrDefault(TimeoutType.TOTAL, DEFAULT_TOTAL_TIMEOUT);
        this.readTimeout = config.getTimeoutOrDefault(TimeoutType.READ, DEFAULT_READ_TIMEOUT);
        this.writeTimeout = config.getTimeoutOrDefault(TimeoutType.WRITE, DEFAULT_WRITE_TIMEOUT);
        this.circuitBreakerTimeout = config.getTimeoutOrDefault(TimeoutType.CIRCUIT_BREAKER, DEFAULT_CIRCUIT_BREAKER_TIMEOUT);
        
        log.debug("创建路由: {} 超时配置 - 连接: {}, 请求: {}, 总计: {}", 
                 id, connectionTimeout, requestTimeout, totalTimeout);
    }
    
    /**
     * 完整构造函数
     */
    public ConfigurableUniversalRoute(String id, String name, String description,
                                    int order, boolean enabled,
                                    List<Protocol> supportedProtocols,
                                    List<UniversalPredicate> predicates,
                                    List<UniversalFilter> filters,
                                    RouteTarget target,
                                    Map<String, Object> metadata,
                                    Duration connectionTimeout,
                                    Duration requestTimeout,
                                    Duration totalTimeout,
                                    Duration readTimeout,
                                    Duration writeTimeout,
                                    Duration circuitBreakerTimeout,
                                    boolean timeoutEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.order = order;
        this.enabled = enabled;
        this.supportedProtocols = supportedProtocols;
        this.predicates = predicates;
        this.filters = filters;
        this.target = target;
        this.metadata = metadata;
        this.timeoutEnabled = timeoutEnabled;
        
        // 超时配置，如果未设置则使用默认值
        this.connectionTimeout = connectionTimeout != null ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT;
        this.requestTimeout = requestTimeout != null ? requestTimeout : DEFAULT_REQUEST_TIMEOUT;
        this.totalTimeout = totalTimeout != null ? totalTimeout : DEFAULT_TOTAL_TIMEOUT;
        this.readTimeout = readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT;
        this.writeTimeout = writeTimeout != null ? writeTimeout : DEFAULT_WRITE_TIMEOUT;
        this.circuitBreakerTimeout = circuitBreakerTimeout != null ? circuitBreakerTimeout : DEFAULT_CIRCUIT_BREAKER_TIMEOUT;
    }
    
    // ========== 基础方法实现 ==========
    
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
        return supportedProtocols;
    }
    
    @Override
    public List<UniversalPredicate> getPredicates() {
        return predicates;
    }
    
    @Override
    public List<UniversalFilter> getFilters() {
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
    public boolean matches(UniversalRequestContext context) {
        // 检查路由是否启用
        if (!enabled) {
            return false;
        }
        
        // 检查协议是否支持
        if (supportedProtocols != null && !supportedProtocols.isEmpty()) {
            Protocol inboundProtocol = context.getInboundProtocol();
            if (inboundProtocol != null && !supportedProtocols.contains(inboundProtocol)) {
                return false;
            }
        }
        
        // 检查所有断言（AND关系）
        if (predicates != null && !predicates.isEmpty()) {
            for (UniversalPredicate predicate : predicates) {
                if (!predicate.test(context)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // ========== 超时配置方法实现 ==========
    
    @Override
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
    
    @Override
    public Duration getRequestTimeout() {
        return requestTimeout;
    }
    
    @Override
    public Duration getTotalTimeout() {
        return totalTimeout;
    }
    
    @Override
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    @Override
    public Duration getWriteTimeout() {
        return writeTimeout;
    }
    
    @Override
    public Duration getCircuitBreakerTimeout() {
        return circuitBreakerTimeout;
    }
    
    @Override
    public boolean isTimeoutEnabled() {
        return timeoutEnabled;
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 获取默认的超时配置
     */
    public static Duration getDefaultTimeout(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> DEFAULT_CONNECTION_TIMEOUT;
            case REQUEST -> DEFAULT_REQUEST_TIMEOUT;
            case TOTAL -> DEFAULT_TOTAL_TIMEOUT;
            case READ -> DEFAULT_READ_TIMEOUT;
            case WRITE -> DEFAULT_WRITE_TIMEOUT;
            case CIRCUIT_BREAKER -> DEFAULT_CIRCUIT_BREAKER_TIMEOUT;
        };
    }
    
    /**
     * 检查超时配置是否合理
     */
    public boolean validateTimeoutConfig() {
        // 请求超时不应该大于总超时
        if (requestTimeout.compareTo(totalTimeout) > 0) {
            log.warn("路由 {} 请求超时 {} 大于总超时 {}", id, requestTimeout, totalTimeout);
            return false;
        }
        
        // 连接超时不应该大于请求超时
        if (connectionTimeout.compareTo(requestTimeout) > 0) {
            log.warn("路由 {} 连接超时 {} 大于请求超时 {}", id, connectionTimeout, requestTimeout);
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "ConfigurableUniversalRoute{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", order=" + order +
                ", connectionTimeout=" + connectionTimeout +
                ", requestTimeout=" + requestTimeout +
                ", totalTimeout=" + totalTimeout +
                '}';
    }
} 