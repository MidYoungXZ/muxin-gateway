package com.muxin.gateway.spring.boot.starter;

import com.muxin.gateway.core.plus.config.*;
import com.muxin.gateway.core.plus.connect.ConnectionPoolConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gateway Spring Boot 配置属性类
 * 直接复用 gateway-core-plus 的配置类，只做简单的包装和映射
 * 
 * @author muxin
 */
@ConfigurationProperties(prefix = "gateway")
@Data
public class GatewaySpringProperties {
    
    /**
     * 是否启用网关
     */
    private boolean enabled = true;
    
    /**
     * 核心配置 - 直接复用 GatewayCoreConfig
     */
    private GatewayCoreConfig core = GatewayCoreConfig.builder().build();
    
    /**
     * 连接池配置 - 直接复用 ConnectionPoolConfig
     */
    private ConnectionPoolConfig connectionPool = ConnectionPoolConfig.defaultConfig();
    
    /**
     * 路由系统配置 - 直接复用 RouteSystemConfig
     */
    private RouteSystemConfig route = RouteSystemConfig.defaultConfig();
    
    /**
     * 过滤器配置 - 直接复用 FilterConfig
     */
    private FilterConfig filter = FilterConfig.defaultConfig();
    
    /**
     * 负载均衡配置 - 直接复用 LoadBalanceConfig
     */
    private LoadBalanceConfig loadBalance = LoadBalanceConfig.defaultConfig();
    
    /**
     * 节点管理配置 - 直接复用 NodeManagerConfig
     */
    private NodeManagerConfig nodeManager = NodeManagerConfig.defaultConfig();
    
    /**
     * 协议转换配置 - 直接复用 ProtocolConverterConfig
     */
    private ProtocolConverterConfig protocolConverter = ProtocolConverterConfig.defaultConfig();
    
    /**
     * 服务器配置 - 直接复用 ServerConfig
     */
    private ServerConfig server = ServerConfig.defaultConfig();
} 