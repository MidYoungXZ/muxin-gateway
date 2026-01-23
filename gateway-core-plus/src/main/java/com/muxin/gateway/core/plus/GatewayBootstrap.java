package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.config.GatewayConfigLoader;
import com.muxin.gateway.core.plus.config.GatewayCoreConfig;
import com.muxin.gateway.core.plus.config.GatewayRouteConfig;
import com.muxin.gateway.core.plus.config.RouteSystemConfig;
import com.muxin.gateway.core.plus.config.ServerConfig;
import com.muxin.gateway.core.plus.connect.ConnectionPoolConfig;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.route.*;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import com.muxin.gateway.core.plus.server.http.HttpServerConfig;
import com.muxin.gateway.core.plus.server.http.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class GatewayBootstrap implements LifeCycle {

    // ========== 配置 ==========
    private GatewayConfig gatewayConfig;
    private GlobalRouteConfig globalRouteConfig;
    private GatewayRouteConfig gatewayRouteConfig;  // 新增：YAML配置文件加载的配置
    private RouteConfigConverter routeConfigConverter;  // 新增：配置转换器

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

        // 1. 加载 YAML 配置文件
        GatewayConfigLoader configLoader = new GatewayConfigLoader();
        try {
            this.gatewayRouteConfig = configLoader.loadConfig();
            log.info("成功加载 gateway-routes.yml 配置文件");
            log.info("配置文件包含 {} 个服务定义, {} 个路由配置",
                    gatewayRouteConfig.getServices() != null ? gatewayRouteConfig.getServices().size() : 0,
                    gatewayRouteConfig.getRoutes() != null ? gatewayRouteConfig.getRoutes().size() : 0);
        } catch (Exception e) {
            log.warn("加载 gateway-routes.yml 失败，将使用默认配置: {}", e.getMessage());
            this.gatewayRouteConfig = null;
        }

        // 2. 初始化路由配置转换器
        this.routeConfigConverter = new RouteConfigConverter();
        log.debug("路由配置转换器初始化完成");

        // 3. 从 YAML 配置中提取功能域配置
        GatewayCoreConfig coreConfig = GatewayCoreConfig.builder().build();
        if (gatewayRouteConfig != null && gatewayRouteConfig.getDomains() != null) {
            // 可以根据 YAML 配置调整 coreConfig
            log.debug("使用 YAML 配置中的功能域配置");
        }

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

        // 注册配置中的路由和服务
        registerRoutesFromConfig();

        log.debug("Core components initialized");
    }

    /**
     * 从配置文件注册路由和服务
     */
    private void registerRoutesFromConfig() {
        if (gatewayRouteConfig == null) {
            log.debug("无YAML配置，跳过路由注册");
            return;
        }

        if (gatewayRouteConfig.getRoutes() == null || gatewayRouteConfig.getRoutes().isEmpty()) {
            log.debug("配置文件中没有路由定义，跳过路由注册");
            return;
        }

        if (gatewayRouteConfig.getServices() == null || gatewayRouteConfig.getServices().isEmpty()) {
            log.warn("配置文件中没有服务定义，但路由引用了服务");
            return;
        }

        try {
            // 构建服务定义映射
            java.util.Map<String,ServiceDefinition> serviceMap =
                gatewayRouteConfig.getServices().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        ServiceDefinition::getId,
                        service -> service,
                        (existing, replacement) -> {
                            log.warn("服务ID重复: {}，将使用第一个定义", existing.getId());
                            return existing;
                        }
                    ));

            // 使用 RouteConfigConverter 转换路由定义
            java.util.List<Route> routes =
                routeConfigConverter.convertToRoutes(gatewayRouteConfig.getRoutes(), serviceMap);

            // 注册路由到 RouteManager
            for (Route route : routes) {
                if (route != null) {
                    routeManager.insert(route);
                    log.info("成功注册路由: {} - {} (优先级: {})",
                            route.getId(), route.getName(), route.getOrder());
                }
            }

            log.info("从配置文件注册了 {} 个路由", routes.size());

        } catch (Exception e) {
            log.error("注册路由配置失败", e);
            throw new RuntimeException("注册路由配置失败", e);
        }
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

    /**
     * 获取 YAML 配置
     * 返回从 gateway-routes.yml 加载的完整配置
     */
    public GatewayRouteConfig getGatewayRouteConfig() {
        return gatewayRouteConfig;
    }

    /**
     * 获取服务定义列表
     */
    public List<ServiceDefinition> getServices() {
        return gatewayRouteConfig != null && gatewayRouteConfig.getServices() != null
                ? gatewayRouteConfig.getServices()
                : List.of();
    }

    /**
     * 获取路由定义列表
     */
    public List<RouteDefinition> getRouteDefinitions() {
        return gatewayRouteConfig != null && gatewayRouteConfig.getRoutes() != null
                ? gatewayRouteConfig.getRoutes()
                : List.of();
    }

} 