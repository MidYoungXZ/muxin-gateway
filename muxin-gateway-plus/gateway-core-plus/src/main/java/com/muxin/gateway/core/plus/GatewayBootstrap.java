package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.config.GatewayCoreConfig;
import com.muxin.gateway.core.plus.config.RouteSystemConfig;
import com.muxin.gateway.core.plus.config.ServerConfig;
import com.muxin.gateway.core.plus.connect.ConnectionPoolConfig;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.route.GlobalRouteConfig;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import com.muxin.gateway.core.plus.server.http.HttpServerConfig;
import com.muxin.gateway.core.plus.server.http.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;

/**
 * 网关引导类
 * 负责网关所有组件的创建、初始化、启动和关闭
 * <p>
 * 已简化架构：
 * - 移除了FilterManager、LoadBalanceManager、PredicateManager
 * - 功能集成到RouteConfigConverter和EnhancedRouteManager中
 * - 支持全局配置和每路由独立实例
 * - 使用带缓存的协议转换管理器
 *
 * @author muxin
 */
@Slf4j
public class GatewayBootstrap implements LifeCycle {

    // ========== 配置 ==========
    private GatewayConfig gatewayConfig;
    private GlobalRouteConfig globalRouteConfig;

    // ========== 核心组件 ==========
    private ConnectionPoolManager connectionPoolManager;
    private RouteManager routeManager;
    private InstanceManager instanceManager;
    private GatewayProcessor gatewayProcessor;

    // ========== 服务器 ==========
    private NettyHttpServer httpServer;

    // ========== 状态管理 ==========
    private volatile boolean initialized = false;
    private volatile boolean running = false;

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        try {
            log.info("Initializing gateway components...");

            // 1. 初始化配置
            initConfigs();

            // 2. 按依赖顺序初始化组件
            initCoreComponents();

            // 3. 初始化网关处理器
            initGatewayProcessor();

            // 4. 初始化服务器
            initServers();

            initialized = true;
            log.info("Gateway components initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize gateway components", e);
            throw new RuntimeException("Gateway initialization failed", e);
        }
    }

    @Override
    public void start() {
        if (!initialized) {
            init();
        }

        if (running) {
            return;
        }

        try {
            log.info("Starting gateway services...");

            // 1. 启动核心组件
            startCoreComponents();

            // 2. 启动网关处理器
            startGatewayProcessor();

            // 3. 启动服务器
            startServers();

            running = true;
            log.info("Gateway services started successfully");

        } catch (Exception e) {
            log.error("Failed to start gateway services", e);
            throw new RuntimeException("Gateway startup failed", e);
        }
    }

    @Override
    public void shutdown() {
        if (!running) {
            return;
        }

        try {
            log.info("Shutting down gateway services...");

            // 按相反顺序关闭组件
            shutdownServers();
            shutdownGatewayProcessor();
            shutdownCoreComponents();

            running = false;
            initialized = false;

            log.info("Gateway services shutdown completed");

        } catch (Exception e) {
            log.error("Error during gateway shutdown", e);
        }
    }

    // ========== 初始化方法 ==========

    private void initConfigs() {
        log.debug("Initializing configurations...");

        // 使用默认配置
        GatewayCoreConfig coreConfig = GatewayCoreConfig.builder().build();
        RouteSystemConfig routeSystemConfig = RouteSystemConfig.defaultConfig();
        ServerConfig serverConfig = ServerConfig.defaultConfig();

        // 创建主配置
        this.gatewayConfig = GatewayConfig.builder()
                .coreConfig(coreConfig)
                .build();

        // 创建全局路由配置
        this.globalRouteConfig = GlobalRouteConfig.defaultConfig();

        log.debug("Configurations initialized");
    }

    private void initCoreComponents() {
        log.debug("Initializing core components...");

        // 连接池管理器
        ConnectionPoolConfig poolConfig = ConnectionPoolConfig.defaultConfig();
        this.connectionPoolManager = new com.muxin.gateway.core.plus.connect.DefaultConnectionPoolManager(poolConfig);
        connectionPoolManager.init();

        // 路由管理器（使用增强版本，支持全局配置）
        this.routeManager = new com.muxin.gateway.core.plus.route.DefaultRouteManager();
        routeManager.init();

        // 节点管理器
        this.instanceManager = new com.muxin.gateway.core.plus.route.service.DefaultInstanceManager();
        instanceManager.init();

        log.debug("Core components initialized");
    }

    private void initGatewayProcessor() {
        log.debug("Initializing gateway processor...");

        this.gatewayProcessor = new GatewayProcessor(
                gatewayConfig,
                connectionPoolManager,
                routeManager,
                instanceManager
        );
        gatewayProcessor.init();
        log.debug("Gateway processor initialized");
    }

    private void initServers() {
        log.debug("Initializing servers...");

        // HTTP服务器配置
        HttpServerConfig httpConfig = HttpServerConfig.builder()
                .build();

        // 创建HTTP服务器
        this.httpServer = new NettyHttpServer(8080, httpConfig, gatewayProcessor);

        log.debug("Servers initialized");
    }

    // ========== 启动方法 ==========

    private void startCoreComponents() {
        log.debug("Starting core components...");

        connectionPoolManager.start();
        routeManager.start();
        instanceManager.start();

        log.debug("Core components started");
    }

    private void startGatewayProcessor() {
        log.debug("Starting gateway processor...");
        gatewayProcessor.start();
        log.debug("Gateway processor started");
    }

    private void startServers() {
        log.debug("Starting servers...");

        // 启动HTTP服务器
        httpServer.start();
        log.info("HTTP server started on port 8080");

        log.debug("Servers started");
    }

    // ========== 关闭方法 ==========

    private void shutdownServers() {
        log.debug("Shutting down servers...");

        if (httpServer != null) {
            httpServer.stop();
        }

        log.debug("Servers shut down");
    }

    private void shutdownGatewayProcessor() {
        log.debug("Shutting down gateway processor...");

        if (gatewayProcessor != null) {
            gatewayProcessor.shutdown();
        }

        log.debug("Gateway processor shut down");
    }

    private void shutdownCoreComponents() {
        log.debug("Shutting down core components...");

        if (instanceManager != null) {
            instanceManager.shutdown();
        }
        if (routeManager != null) {
            routeManager.shutdown();
        }
        if (connectionPoolManager != null) {
            connectionPoolManager.shutdown();
        }

        log.debug("Core components shut down");
    }

    // ========== 公共方法 ==========

    /**
     * 获取路由管理器
     */
    public RouteManager getRouteManager() {
        return routeManager;
    }

    /**
     * 获取全局路由配置
     */
    public GlobalRouteConfig getGlobalRouteConfig() {
        return globalRouteConfig;
    }

    /**
     * 获取连接池管理器
     */
    public ConnectionPoolManager getConnectionPoolManager() {
        return connectionPoolManager;
    }

    /**
     * 获取节点管理器
     */
    public InstanceManager getNodeManager() {
        return instanceManager;
    }

} 