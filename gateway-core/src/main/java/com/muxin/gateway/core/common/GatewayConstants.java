package com.muxin.gateway.core.common;

import org.asynchttpclient.Response;

/**
 * 网关常量类
 */
public final class GatewayConstants {

    // === 私有构造器，防止实例化 ===
    private GatewayConstants() {
        throw new IllegalStateException("Utility class");
    }

    // === 断言工厂名称 ===
    public static final String PATH_PREDICATE_NAME = "Path";
    public static final String METHOD_PREDICATE_NAME = "Method";

    // === 过滤器工厂名称 ===
    public static final String STRIP_PREFIX_FILTER_NAME = "StripPrefix";

    // === 断言参数名 ===
    public static final String PATTERN_ARG = "pattern";
    public static final String METHODS_ARG = "methods";
    public static final String METHOD_ARG = "method";

    // === 过滤器参数名 ===
    public static final String PARTS_ARG = "parts";

    // === 通用参数键 ===
    public static final String GENKEY_PREFIX = "_genkey_";

    // === 负载均衡器名称 ===
    public static final String ROUND_ROBIN_BALANCER = "Round";

    // === 默认值 ===
    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String DEFAULT_NAMESPACE = "DEFAULT";

    // === 路由相关 ===
    public static final String ROUTE_404_ID = "404";
    public static final String ROUTE_404_PATH = "/";

    // === Exchange 属性键 ===
    public static final String GATEWAY_REQUEST_URL_ATTR = "gatewayRequestUrl";
    public static final String GATEWAY_LOADBALANCER_RESPONSE_ATTR = "gatewayLoadBalancerResponse";
    public static final String GATEWAY_STRIPPED_PATH_ATTR = "gatewayStrippedPath";

    // === 配置前缀 ===
    public static final String GATEWAY_CONFIG_PREFIX = "muxin.gateway";
    public static final String NACOS_REGISTER_TYPE = "nacos";

    // === HTTP 相关 ===
    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";



    // 服务ID
    public static final String SERVICE_ID = "SERVICE_ID";
    
    // 元数据键
    public static final String META_DATA_KEY = "META_DATA";
    
    // 原始请求URL属性
    public static final String GATEWAY_ORIGINAL_REQUEST_URL_ATTR = "gateway.original.request.url";
    
    // 请求路径属性
    public static final String GATEWAY_REQUEST_PATH_ATTR = "gateway.request.path";
    
    // 路由ID属性
    public static final String GATEWAY_ROUTE_ID_ATTR = "gateway.route.id";
    
    // 服务实例属性
    public static final String GATEWAY_SERVICE_INSTANCE_ATTR = "gateway.service.instance";
    
    // 请求开始时间
    public static final String GATEWAY_REQUEST_START_TIME_ATTR = "gateway.request.start.time";
    
    // 请求ID
    public static final String GATEWAY_REQUEST_ID_ATTR = "gateway.request.id";
    
    // 默认超时时间（毫秒）
    public static final int DEFAULT_TIMEOUT = 30000;
    
    // 默认连接超时时间（毫秒）
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    
    // 默认最大连接数
    public static final int DEFAULT_MAX_CONNECTIONS = 1000;
    
    // 默认每个主机最大连接数
    public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 100;
    
    // 负载均衡类型
    public static final String LOAD_BALANCE_RANDOM = "Random";
    public static final String LOAD_BALANCE_WEIGHTED_ROUND_ROBIN = "WeightedRound";
    public static final String LOAD_BALANCE_LEAST_CONNECTIONS = "LeastConnections";
    
    // HTTP头名称
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    public static final String X_FORWARDED_PORT = "X-Forwarded-Port";
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String X_GATEWAY_REQUEST_TIME = "X-Gateway-Request-Time";
    
    // HTTP状态码
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    
    // 响应消息
    public static final String RESPONSE_NOT_FOUND = "Not Found";
    public static final String RESPONSE_INTERNAL_ERROR = "Internal Server Error";
    public static final String RESPONSE_BAD_GATEWAY = "Bad Gateway";
    public static final String RESPONSE_SERVICE_UNAVAILABLE = "Service Unavailable";
    public static final String RESPONSE_GATEWAY_TIMEOUT = "Gateway Timeout";
}
