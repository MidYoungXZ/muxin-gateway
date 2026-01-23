package com.muxin.gateway.core.plus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.muxin.gateway.core.plus.route.ServiceDefinition;
import com.muxin.gateway.core.plus.route.RouteDefinition;
import com.muxin.gateway.core.plus.route.TimeoutConfig;
import com.muxin.gateway.core.plus.route.predicate.PredicateDefinition;
import com.muxin.gateway.core.plus.route.filter.FilterDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 完整的网关路由配置类
 * 对应YAML配置文件的整体结构（v2.0）
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRouteConfig {
    
    /**
     * 功能域配置
     */
    private DomainsConfig domains;
    
    /**
     * 枚举定义
     */
    private EnumsConfig enums;
    
    /**
     * 服务定义（独立资源）
     */
    private List<ServiceDefinition> services;
    
    /**
     * 路由配置列表
     */
    private List<RouteDefinition> routes;
    
    /**
     * 全局过滤器配置
     */
    private List<GlobalFilterConfig> globalFilters;
    
    /**
     * 负载均衡策略配置
     */
    private Map<String, LoadBalanceStrategyConfig> loadBalanceStrategies;
    
    /**
     * 全局路由配置
     */
    private GlobalRouteConfig globalRouteConfig;
    
    /**
     * 协议配置
     */
    private Map<String, ProtocolConfig> protocols;
    
    /**
     * 注册中心配置
     */
    private RegistryConfig registries;
    
    /**
     * 监控配置
     */
    private MonitoringConfig monitoring;
    
    /**
     * 安全配置
     */
    private SecurityConfig security;
    
    /**
     * 缓存配置
     */
    private CacheConfig cache;
    
    /**
     * 缓存规则配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfig {
        private boolean enabled;
        private String provider;
        private String defaultTtl;
        private int maxSize;
        private CacheRuleConfig routeCache;
        private CacheRuleConfig authCache;
    }
    
    /**
     * 路由定义配置构建器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDefinitionBuilder {
        
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
        private int order;
        
        /**
         * 是否启用
         */
        private boolean enabled;
        
        /**
         * 协议类型
         */
        private String protocol;
        
        /**
         * 服务引用（引用 services 中的服务ID）
         */
        private String serviceRef;
        
        /**
         * 断言配置列表（AND关系）
         */
        private List<PredicateDefinition> predicates;
        
        /**
         * 过滤器配置列表
         */
        private List<FilterDefinition> filters;
        
        /**
         * 负载均衡配置（路由级别）
         */
        private LoadBalanceDefinition loadBalance;
        
        /**
         * 超时配置
         */
        private TimeoutConfig timeouts;
        
        /**
         * 路由元数据
         */
        private Map<String, Object> metadata;
        
        /**
         * 构建RouteDefinition对象
         */
        public RouteDefinition build() {
            RouteDefinition definition = new RouteDefinition();
            definition.setId(this.id);
            definition.setName(this.name);
            definition.setDescription(this.description);
            definition.setOrder(this.order);
            definition.setEnabled(this.enabled);
            definition.setProtocol(this.protocol);
            definition.setServiceRef(this.serviceRef);
            definition.setPredicates(this.predicates);
            definition.setFilters(this.filters);
            definition.setLoadBalance(this.loadBalance);
            definition.setTimeouts(this.timeouts);
            definition.setMetadata(this.metadata);
            return definition;
        }
    }

    /**
     * 功能域配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainsConfig {
        private CoreConfig core;
        private ThreadPoolConfig threadPools;
        private ServerConfig servers;
        private ConnectionPoolConfig connectionPools;
    }

    /**
     * 枚举配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumsConfig {
        private List<String> protocolType;
        private List<String> filterType;
        private List<String> loadBalanceStrategy;
        private List<String> predicateType;
        private List<String> httpMethod;
    }
    
    /**
     * 核心配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoreConfig {
        private String defaultTimeout;
        private String maxRequestSize;
        private String maxResponseSize;
    }
    
    /**
     * 线程池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadPoolConfig {
        private BusinessThreadPoolConfig business;
    }
    
    /**
     * 业务线程池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessThreadPoolConfig {
        private int coreSize;
        private int maxSize;
        private int queueCapacity;
        private String keepAlive;
    }
    
    /**
     * 服务器配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerConfig {
        private HttpServerConfig http;
        private ManagementConfig management;
    }
    
    /**
     * HTTP服务器配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpServerConfig {
        private int port;
        private String maxContentLength;
        private boolean keepAlive;
        private boolean compression;
    }
    
    /**
     * 管理配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagementConfig {
        private int port;
        private boolean enabled;
    }
    
    /**
     * 连接池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionPoolConfig {
        @JsonProperty("default")
        private DefaultConnectionPoolConfig defaultConfig;
    }

    /**
     * 默认连接池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultConnectionPoolConfig {
        private int maxConnectionsPerHost;
        private int maxIdleConnections;
        private String connectionTimeout;
        private String idleTimeout;
    }
    
    /**
     * 全局过滤器配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalFilterConfig {
        private String type;
        private int order;
        private boolean enabled;
        private Map<String, Object> config;
    }
    
    /**
     * 负载均衡策略配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadBalanceStrategyConfig {
        /**
         * 策略实现类全限定名
         * 使用 @JsonProperty 注解显式映射 YAML 中的 "class" 字段
         */
        @JsonProperty("class")
        private String className;

        /**
         * 策略配置参数
         */
        private Map<String, Object> config;
    }
    
    /**
     * 全局路由配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalRouteConfig {
        private DefaultLoadBalanceConfig defaultLoadBalance;
    }
    
    /**
     * 默认负载均衡配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultLoadBalanceConfig {
        private String strategy;
        private Map<String, Object> config;
    }
    
    /**
     * 协议配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProtocolConfig {
        private List<String> versions;
        private String defaultVersion;
        private boolean keepAlive;
        private boolean compression;
        private Integer maxHeaderSize;
        private Integer maxChunkSize;
        private Integer maxFrameSize;
        private String heartbeatInterval;
        private boolean tcpNoDelay;
        private String soTimeout;
        private String maxMessageSize;
    }
    
    /**
     * 注册中心配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistryConfig {
        /**
         * 默认注册中心类型
         * 使用 @JsonProperty 映射 YAML 中的 "default" 字段
         */
        @JsonProperty("default")
        private String defaultRegistry;

        private NacosConfig nacos;
        private EurekaConfig eureka;
        private ConsulConfig consul;
    }
    
    /**
     * Nacos配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NacosConfig {
        private String serverAddr;
        private String namespace;
        private String group;
        private String username;
        private String password;
    }
    
    /**
     * Eureka配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EurekaConfig {
        private String serviceUrl;
        private boolean preferIpAddress;
    }
    
    /**
     * Consul配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsulConfig {
        private String host;
        private int port;
    }
    
    /**
     * 监控配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitoringConfig {
        private MetricsConfig metrics;
        private TracingConfig tracing;
        private LoggingConfig logging;
    }
    
    /**
     * 指标配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsConfig {
        private boolean enabled;
        private String exportInterval;
        private Map<String, String> tags;
    }
    
    /**
     * 链路追踪配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TracingConfig {
        private boolean enabled;
        private double samplingRate;
        private String traceHeader;
        private String spanHeader;
    }
    
    /**
     * 日志配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggingConfig {
        private Map<String, String> level;
        private String pattern;
    }
    
    /**
     * 安全配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityConfig {
        private JwtConfig jwt;
        private RateLimitConfig rateLimit;
    }
    
    /**
     * JWT配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtConfig {
        private String secret;
        private String expiration;
        private String refreshExpiration;
        private String issuer;
    }
    
    /**
     * 限流配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitConfig {
        private RateLimitRule defaultRule;
        private RateLimitRule byIp;
        private RateLimitRule byUser;
    }
    
    /**
     * 限流规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitRule {
        private int requestsPerSecond;
        private int burstCapacity;
    }
    
    /**
     * 缓存规则配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheRuleConfig {
        private boolean enabled;
        private String ttl;
        private int maxSize;
    }
}