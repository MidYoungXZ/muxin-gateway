package com.muxin.gateway.core;

import com.muxin.gateway.core.common.LifeCycle;
import com.muxin.gateway.core.config.GatewayConfig;
import com.muxin.gateway.core.config.GatewayConfigLoader;
import com.muxin.gateway.core.config.GatewayCoreConfig;
import com.muxin.gateway.core.config.GatewayRouteConfig;
import com.muxin.gateway.core.config.RouteSystemConfig;
import com.muxin.gateway.core.config.ServerConfig;
import com.muxin.gateway.core.config.provider.ConfigChangedEvent;
import com.muxin.gateway.core.config.provider.ConfigChangeListener;
import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.config.provider.ServiceConfigProvider;
import com.muxin.gateway.core.connect.ConnectionPoolManager;
import com.muxin.gateway.core.connect.netty.NettyConnectionPoolManager;
import com.muxin.gateway.core.connect.netty.NettyPoolConfig;
import com.muxin.gateway.core.route.*;
import com.muxin.gateway.core.route.filter.FilterDefinition;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategyFactory;
import com.muxin.gateway.core.route.predicate.PathPredicate;
import com.muxin.gateway.core.route.predicate.Predicate;
import com.muxin.gateway.core.service.DefaultServiceRegistry;
import com.muxin.gateway.core.service.ServiceRegistry;
import com.muxin.gateway.core.server.HttpServerConfig;
import com.muxin.gateway.core.server.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private GatewayRouteConfig gatewayRouteConfig;
    private RouteConfigConverter routeConfigConverter;
    private RouteConfigProvider routeConfigProvider;
    private ServiceConfigProvider serviceConfigProvider;

    // ========== 核心组件 ==========
    private ConnectionPoolManager connectionPoolManager;
    private RouteManager routeManager;
    private ServiceRegistry serviceRegistry;
    private GatewayProcessor gatewayProcessor;

    // ========== 服务器 ==========
    private NettyHttpServer httpServer;
    private int serverPort = 8080;
    private HttpServerConfig httpServerConfig;

    // ========== 状态管理 ==========
    private volatile boolean initialized = false;
    private volatile boolean running = false;

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public void setHttpServerConfig(HttpServerConfig config) {
        this.httpServerConfig = config;
    }

    public void setRouteConfigProvider(RouteConfigProvider routeConfigProvider) {
        this.routeConfigProvider = routeConfigProvider;
    }

    public void setServiceConfigProvider(ServiceConfigProvider serviceConfigProvider) {
        this.serviceConfigProvider = serviceConfigProvider;
    }

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

        this.routeConfigConverter = new RouteConfigConverter();
        log.debug("路由配置转换器初始化完成");

        GatewayCoreConfig coreConfig = GatewayCoreConfig.builder().build();
        RouteSystemConfig routeSystemConfig = RouteSystemConfig.defaultConfig();
        ServerConfig serverConfig = ServerConfig.defaultConfig();

        this.gatewayConfig = GatewayConfig.builder()
                .coreConfig(coreConfig)
                .build();

        this.globalRouteConfig = GlobalRouteConfig.defaultConfig();

        log.debug("Configurations initialized");
    }

    private void initCoreComponents() {
        log.debug("Initializing core components...");

        NettyPoolConfig poolConfig = NettyPoolConfig.defaultConfig();
        this.connectionPoolManager = new NettyConnectionPoolManager(poolConfig);
        connectionPoolManager.init();

        // 路由管理器（使用增强版本，支持全局配置）
        this.routeManager = new DefaultRouteManager();
        routeManager.init();

        // 节点管理器
        this.serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.init();
        
        // 注入 ServiceRegistry 到 RouteConfigConverter
        if (routeConfigConverter != null) {
            routeConfigConverter.setServiceRegistry(serviceRegistry);
        }

        // 注册配置中的路由和服务
        registerRoutesFromConfig();

        log.debug("Core components initialized");
    }

    /**
     * 从配置文件注册路由和服务
     */
    private void registerRoutesFromConfig() {
        boolean hasRoutes = false;

        if (routeConfigProvider != null && serviceConfigProvider != null) {
            try {
                List<ServiceDefinition> services = serviceConfigProvider.getServices();
                Map<String, ServiceDefinition> serviceMap = services.stream()
                        .collect(Collectors.toMap(
                                ServiceDefinition::getId,
                                service -> service,
                                (existing, replacement) -> {
                                    log.warn("服务ID重复: {}，将使用第一个定义", existing.getId());
                                    return existing;
                                }
                        ));

                List<RouteDefinition> routeDefinitions = routeConfigProvider.getRoutes();

                List<Route> routes = routeConfigConverter.convertToRoutes(routeDefinitions, serviceMap);

                for (Route route : routes) {
                    if (route != null) {
                        routeManager.insert(route);
                        log.info("成功注册路由: {} - {} (优先级: {})",
                                route.getId(), route.getName(), route.getOrder());
                    }
                }

                log.info("从 {} 注册了 {} 个路由", routeConfigProvider.getSource(), routes.size());
                hasRoutes = !routes.isEmpty();

                routeConfigProvider.addChangeListener(new ConfigChangeListener() {
                    @Override
                    public void onRouteConfigChanged(ConfigChangedEvent event) {
                        if (log.isInfoEnabled()) {
                            log.info("Route config changed: {}, refreshing routes", event.getChangeType());
                        }
                        refreshRoutes();
                    }

                    @Override
                    public void onServiceConfigChanged(ConfigChangedEvent event) {
                        if (log.isInfoEnabled()) {
                            log.info("Service config changed: {}, refreshing routes", event.getChangeType());
                        }
                        refreshRoutes();
                    }
                });

            } catch (Exception e) {
                log.error("注册路由配置失败", e);
                throw new RuntimeException("注册路由配置失败", e);
            }
        } else if (gatewayRouteConfig != null && gatewayRouteConfig.getRoutes() != null && !gatewayRouteConfig.getRoutes().isEmpty()) {
            if (gatewayRouteConfig.getServices() == null || gatewayRouteConfig.getServices().isEmpty()) {
                log.warn("配置文件中没有服务定义，但路由引用了服务");
            } else {
                try {
                    Map<String,ServiceDefinition> serviceMap =
                        gatewayRouteConfig.getServices().stream()
                            .collect(Collectors.toMap(
                                ServiceDefinition::getId,
                                service -> service,
                                (existing, replacement) -> {
                                    log.warn("服务ID重复: {}，将使用第一个定义", existing.getId());
                                    return existing;
                                }
                            ));

                    if (gatewayRouteConfig.getGlobalFilters() != null && !gatewayRouteConfig.getGlobalFilters().isEmpty()) {
                        List<FilterDefinition> globalFilters =
                            gatewayRouteConfig.getGlobalFilters().stream()
                                .map(gfc -> FilterDefinition.builder()
                                    .type(gfc.getType())
                                    .order(gfc.getOrder())
                                    .enabled(gfc.isEnabled())
                                    .config(gfc.getConfig())
                                    .build())
                                .toList();
                        routeConfigConverter.setGlobalFilters(globalFilters);
                        log.info("已加载全局过滤器配置: {} 个", globalFilters.size());
                    }

                    List<Route> routes =
                        routeConfigConverter.convertToRoutes(gatewayRouteConfig.getRoutes(), serviceMap);

                    for (Route route : routes) {
                        if (route != null) {
                            routeManager.insert(route);
                            log.info("成功注册路由: {} - {} (优先级: {})",
                                    route.getId(), route.getName(), route.getOrder());
                        }
                    }

                    log.info("从配置文件注册了 {} 个路由", routes.size());
                    hasRoutes = !routes.isEmpty();

                } catch (Exception e) {
                    log.error("注册路由配置失败", e);
                    throw new RuntimeException("注册路由配置失败", e);
                }
            }
        }

        if (!hasRoutes) {
            log.info("配置文件中没有路由定义，创建默认兜底路由");
            Route defaultRoute = createDefaultFallbackRoute();
            routeManager.insert(defaultRoute);
            routeManager.setDefaultRoute(defaultRoute);
            log.info("默认兜底路由已创建: {}", defaultRoute.getId());
        }

        if (routeManager.getDefaultRoute() == null) {
            log.info("未设置默认路由，创建默认兜底路由");
            Route defaultRoute = createDefaultFallbackRoute();
            routeManager.setDefaultRoute(defaultRoute);
        }
    }

    private void refreshRoutes() {
        try {
            if (routeConfigProvider == null || serviceConfigProvider == null) {
                return;
            }

            List<ServiceDefinition> services = serviceConfigProvider.getServices();
            Map<String, ServiceDefinition> serviceMap = services.stream()
                    .collect(Collectors.toMap(ServiceDefinition::getId, s -> s));

            List<RouteDefinition> routeDefinitions = routeConfigProvider.getRoutes();
            List<Route> routes = routeConfigConverter.convertToRoutes(routeDefinitions, serviceMap);

            routeManager.clear();
            for (Route route : routes) {
                if (route != null) {
                    routeManager.insert(route);
                }
            }

            log.info("Routes refreshed: {} routes loaded from {}", routes.size(), routeConfigProvider.getSource());
        } catch (Exception e) {
            log.error("Failed to refresh routes", e);
        }
    }

    /**
     * 创建默认兜底路由
     */
    private Route createDefaultFallbackRoute() {
        ServiceDefinition defaultService = ServiceDefinition.builder()
                .id("default-service")
                .name("default-service")
                .type(ServiceType.STATIC)
                .supportedProtocols(List.of("HTTP"))
                .addresses(List.of(
                        AddressDefinition.builder()
                                .uri("http://127.0.0.1:8080")
                                .weight(100)
                                .build()
                ))
                .build();

        RouteService defaultRouteService = new StaticRouteService(
                defaultService,
                serviceRegistry
        );

        Predicate pathPredicate = new PathPredicate("/**");

        LoadBalanceStrategy loadBalanceStrategy = LoadBalanceStrategyFactory.createStrategy(null);

        return DefaultRoute.builder()
                .id("default-fallback-route")
                .name("默认兜底路由")
                .description("处理未匹配到其他路由的请求")
                .order(Integer.MAX_VALUE)
                .enabled(true)
                .predicates(List.of(pathPredicate))
                .filters(Collections.emptyList())
                .service(defaultRouteService)
                .loadBalanceStrategy(loadBalanceStrategy)
                .build();
    }

    private void initGatewayProcessor() {
        log.debug("Initializing gateway processor...");

        this.gatewayProcessor = new GatewayProcessor(
                gatewayConfig,
                connectionPoolManager,
                routeManager,
                serviceRegistry
        );
        gatewayProcessor.init();
        log.debug("Gateway processor initialized");
    }

    private void initServers() {
        log.debug("Initializing servers...");

        HttpServerConfig httpConfig = this.httpServerConfig;
        if (httpConfig == null) {
            httpConfig = HttpServerConfig.builder().build();
        }

        this.httpServer = new NettyHttpServer(serverPort, httpConfig, gatewayProcessor);

        log.debug("Servers initialized on port {}", serverPort);
    }

    // ========== 启动方法 ==========

    private void startCoreComponents() {
        log.debug("Starting core components...");

        connectionPoolManager.start();
        routeManager.start();
        serviceRegistry.start();

        log.debug("Core components started");
    }

    private void startGatewayProcessor() {
        log.debug("Starting gateway processor...");
        gatewayProcessor.start();
        log.debug("Gateway processor started");
    }

    private void startServers() {
        log.debug("Starting servers...");

        httpServer.start();
        log.info("HTTP server started on port {}", serverPort);

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

        if (serviceRegistry != null) {
            serviceRegistry.shutdown();
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
     * 获取服务注册中心
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
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