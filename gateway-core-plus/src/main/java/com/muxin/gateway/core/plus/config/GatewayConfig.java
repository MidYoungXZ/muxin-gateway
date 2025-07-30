package com.muxin.gateway.core.plus.config;

import com.muxin.gateway.core.plus.connect.ConnectionPoolConfig;
import lombok.Builder;
import lombok.Data;

/**
 * 网关统一配置类
 * 整合所有组件的配置信息
 *
 * @author muxin
 */
@Data
@Builder
public class GatewayConfig {

    // ========== 核心网关配置 ==========
    @Builder.Default
    private GatewayCoreConfig coreConfig = GatewayCoreConfig.defaultConfig();

    // ========== 各组件配置 ==========
    @Builder.Default
    private ConnectionPoolConfig connectionPoolConfig = ConnectionPoolConfig.defaultConfig();

    @Builder.Default
    private RouteSystemConfig routeConfig = RouteSystemConfig.defaultConfig();

    @Builder.Default
    private FilterConfig filterConfig = FilterConfig.defaultConfig();

    @Builder.Default
    private LoadBalanceConfig loadBalanceConfig = LoadBalanceConfig.defaultConfig();

    @Builder.Default
    private NodeManagerConfig nodeManagerConfig = NodeManagerConfig.defaultConfig();

    @Builder.Default
    private ProtocolConverterConfig protocolConverterConfig = ProtocolConverterConfig.defaultConfig();

    @Builder.Default
    private ServerConfig serverConfig = ServerConfig.defaultConfig();

    /**
     * 创建默认配置
     */
    public static GatewayConfig defaultConfig() {
        return GatewayConfig.builder().build();
    }

    /**
     * 创建开发环境配置
     */
    public static GatewayConfig developmentConfig() {
        return GatewayConfig.builder()
                .coreConfig(GatewayCoreConfig.builder()
                        .enableTracing(true)
                        .enableMetrics(true)
                        .workerThreads(4)
                        .build())
                .connectionPoolConfig(ConnectionPoolConfig.builder()
                        .maxConnectionsPerTarget(5)
                        .minConnectionsPerTarget(1)
                        .enableWarmup(false)
                        .build())
                .serverConfig(ServerConfig.builder()
                        .httpPort(8080)
                        .enableSsl(false)
                        .build())
                .build();
    }

    /**
     * 创建生产环境配置
     */
    public static GatewayConfig productionConfig() {
        return GatewayConfig.builder()
                .coreConfig(GatewayCoreConfig.builder()
                        .enableTracing(true)
                        .enableMetrics(true)
                        .workerThreads(Runtime.getRuntime().availableProcessors() * 2)
                        .build())
                .connectionPoolConfig(ConnectionPoolConfig.builder()
                        .maxConnectionsPerTarget(20)
                        .minConnectionsPerTarget(5)
                        .enableWarmup(true)
                        .enableHealthCheck(true)
                        .build())
                .serverConfig(ServerConfig.builder()
                        .httpPort(8080)
                        .httpsPort(8443)
                        .enableSsl(true)
                        .backlog(2048)
                        .build())
                .build();
    }

    /**
     * 验证配置的有效性
     */
    public void validate() {
        if (coreConfig == null) {
            throw new IllegalArgumentException("coreConfig不能为空");
        }
        if (connectionPoolConfig == null) {
            throw new IllegalArgumentException("connectionPoolConfig不能为空");
        }
        if (serverConfig == null) {
            throw new IllegalArgumentException("serverConfig不能为空");
        }
        if (routeConfig == null) {
            throw new IllegalArgumentException("routeConfig不能为空");
        }

        // 验证各个配置的有效性
        coreConfig.validate();
        serverConfig.validate();
        routeConfig.validate();
        filterConfig.validate();
        loadBalanceConfig.validate();
        nodeManagerConfig.validate();
        protocolConverterConfig.validate();
    }
} 