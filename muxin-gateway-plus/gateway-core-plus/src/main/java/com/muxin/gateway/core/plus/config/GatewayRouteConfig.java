package com.muxin.gateway.core.plus.config;

import com.muxin.gateway.core.plus.route.RouteDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 完整的网关路由配置类
 * 对应YAML配置文件的整体结构
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRouteConfig {
    
    /**
     * 网关核心配置
     */
    private GatewayConfig gateway;
    
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
     * 网关配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayConfig {
        private CoreConfig core;
        private ServerConfig server;
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
        private ThreadPoolConfig businessThreadPool;
        private ConnectionPoolConfig connectionPool;
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
     * 线程池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadPoolConfig {
        private int coreSize;
        private int maxSize;
        private int queueCapacity;
        private String keepAlive;
    }
    
    /**
     * 连接池配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionPoolConfig {
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
        private String className;
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
     * 缓存配置
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