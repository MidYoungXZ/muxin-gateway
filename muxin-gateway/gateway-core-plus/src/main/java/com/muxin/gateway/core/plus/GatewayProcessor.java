package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.UniversalRoute;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.filter.FilterManager;
import com.muxin.gateway.core.plus.loadbalance.LoadBalanceManager;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.ProtocolConverterManager;
import com.muxin.gateway.core.plus.node.NodeManager;
import com.muxin.gateway.core.plus.node.UniversalServiceNode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 网关处理器抽象类
 * 定义了网关处理请求的标准流程，使用模板方法模式
 *
 * @author muxin
 */
@Slf4j
public abstract class GatewayProcessor implements LifeCycle {

    // ========== 核心组件依赖 ==========
    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final FilterManager filterManager;
    protected final LoadBalanceManager loadBalanceManager;
    protected final NodeManager nodeManager;
    protected final ProtocolConverterManager protocolConverterManager;

    // ========== 状态管理 ==========
    protected volatile boolean running = false;

    protected GatewayProcessor(GatewayConfig config,
                               ConnectionPoolManager connectionPoolManager,
                               RouteManager routeManager,
                               FilterManager filterManager,
                               LoadBalanceManager loadBalanceManager,
                               NodeManager nodeManager,
                               ProtocolConverterManager protocolConverterManager) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.filterManager = filterManager;
        this.loadBalanceManager = loadBalanceManager;
        this.nodeManager = nodeManager;
        this.protocolConverterManager = protocolConverterManager;

        log.info("[GatewayProcessor] 网关处理器创建完成");
    }

    /**
     * 处理入站请求 - 模板方法
     * 定义了完整的10步请求处理流程
     */
    public final CompletableFuture<Message> processRequest(ServerConnection serverConnection, Message request) {
        long startTime = System.currentTimeMillis();
        String requestId = request.getMessageId();

        log.debug("[GatewayProcessor] 开始处理请求: {}", requestId);

        return CompletableFuture
                // ========== 第1步：请求接收与验证 ==========
                .supplyAsync(() -> {
                    log.debug("[GatewayProcessor] 步骤1：请求接收与验证 - {}", requestId);
                    return validateRequest(serverConnection, request);
                })

                // ========== 第2步：协议转换（入站）==========
                .thenCompose(validatedRequest -> {
                    log.debug("[GatewayProcessor] 步骤2：协议转换（入站）- {}", requestId);
                    return convertInboundProtocol(validatedRequest);
                })

                // ========== 第3步：路由匹配 ==========
                .thenCompose(convertedRequest -> {
                    log.debug("[GatewayProcessor] 步骤3：路由匹配 - {}", requestId);
                    return matchRoute(convertedRequest);
                })

                // ========== 第4步：过滤器处理（前置）==========
                .thenCompose(routeResult -> {
                    log.debug("[GatewayProcessor] 步骤4：过滤器处理（前置）- {}", requestId);
                    return executePreFilters(routeResult.getRoute(), routeResult.getRequest());
                })

                // ========== 第5步：负载均衡与节点选择 ==========
                .thenCompose(filteredRequest -> {
                    log.debug("[GatewayProcessor] 步骤5：负载均衡与节点选择 - {}", requestId);
                    return selectTargetNode(filteredRequest);
                })

                // ========== 第6步：连接管理 ==========
                .thenCompose(nodeResult -> {
                    log.debug("[GatewayProcessor] 步骤6：连接管理 - {}", requestId);
                    return acquireConnection(nodeResult.getNode(), nodeResult.getRequest());
                })

                // ========== 第7步：后端服务调用（异步开始）==========
                .thenCompose(connectionResult -> {
                    log.debug("[GatewayProcessor] 步骤7：后端服务调用 - {}", requestId);
                    return invokeBackendService(connectionResult.getConnection(), connectionResult.getRequest());
                })

                // ========== 第8步：协议转换（出站）==========
                .thenCompose(response -> {
                    log.debug("[GatewayProcessor] 步骤8：协议转换（出站）- {}", requestId);
                    return convertOutboundProtocol(response, serverConnection);
                })

                // ========== 第9步：过滤器处理（后置）==========
                .thenCompose(convertedResponse -> {
                    log.debug("[GatewayProcessor] 步骤9：过滤器处理（后置）- {}", requestId);
                    return executePostFilters(convertedResponse);
                })

                // ========== 第10步：响应返回 ==========
                .thenCompose(filteredResponse -> {
                    log.debug("[GatewayProcessor] 步骤10：响应返回 - {}", requestId);
                    return sendResponse(serverConnection, filteredResponse);
                })

                // ========== 异常处理 ==========
                .exceptionally(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("[GatewayProcessor] 请求处理失败: {} - 耗时: {}ms", requestId, duration, throwable);
                    return handleError(serverConnection, request, throwable);
                })

                // ========== 完成处理 ==========
                .whenComplete((result, throwable) -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if (throwable == null) {
                        log.info("[GatewayProcessor] 请求处理完成: {} - 耗时: {}ms", requestId, duration);
                    }
                });
    }
    
    /**
     * 处理入站请求 - process方法别名
     */
    public final CompletableFuture<Message> process(ServerConnection serverConnection, Message request) {
        return processRequest(serverConnection, request);
    }

    // ========== 模板方法定义（子类可以覆盖）==========

    /**
     * 第1步：请求接收与验证
     */
    protected Message validateRequest(ServerConnection serverConnection, Message request) {
        // 默认实现：直接返回请求
        return request;
    }

    /**
     * 第2步：协议转换（入站）
     */
    protected CompletableFuture<Message> convertInboundProtocol(Message request) {
        // 默认实现：直接返回请求（无需转换）
        return CompletableFuture.completedFuture(request);
    }

    /**
     * 第3步：路由匹配
     */
    protected CompletableFuture<RouteMatchResult> matchRoute(Message request) {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: 实现真正的路由匹配逻辑
            // 临时创建默认路由
            log.debug("[GatewayProcessor] 路由匹配（临时实现）");
            return new RouteMatchResult(createDefaultRoute(), request);
        });
    }

    /**
     * 第4步：过滤器处理（前置）
     */
    protected CompletableFuture<Message> executePreFilters(UniversalRoute route, Message request) {
        return CompletableFuture.completedFuture(request); // 临时跳过过滤器
    }

    /**
     * 第5步：负载均衡与节点选择
     */
    protected CompletableFuture<NodeSelectionResult> selectTargetNode(Message request) {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: 实现真正的负载均衡逻辑
            // 临时创建默认节点
            log.debug("[GatewayProcessor] 负载均衡（临时实现）");
            return new NodeSelectionResult(createDefaultNode(), request);
        });
    }

    /**
     * 第6步：连接管理
     */
    protected CompletableFuture<ConnectionAcquisitionResult> acquireConnection(UniversalServiceNode node, Message request) {
        return connectionPoolManager.getClientConnection(node.getAddress(), request.getProtocol())
                .thenApply(connection -> new ConnectionAcquisitionResult(connection, request));
    }

    /**
     * 第7步：后端服务调用
     */
    protected abstract CompletableFuture<Message> invokeBackendService(ClientConnection connection, Message request);

    /**
     * 第8步：协议转换（出站）
     */
    protected CompletableFuture<Message> convertOutboundProtocol(Message response, ServerConnection serverConnection) {
        // 默认实现：直接返回响应（无需转换）
        return CompletableFuture.completedFuture(response);
    }

    /**
     * 第9步：过滤器处理（后置）
     */
    protected CompletableFuture<Message> executePostFilters(Message response) {
        return CompletableFuture.completedFuture(response); // 临时跳过过滤器
    }

    /**
     * 第10步：响应返回
     */
    protected CompletableFuture<Message> sendResponse(ServerConnection serverConnection, Message response) {
        return serverConnection.sendResponse(response)
                .thenApply(v -> response);
    }

    /**
     * 异常处理
     */
    protected Message handleError(ServerConnection serverConnection, Message request, Throwable throwable) {
        log.error("[GatewayProcessor] 处理错误", throwable);

        // 发送错误响应
        serverConnection.sendError(throwable);

        // 返回错误响应消息
        Message errorResponse = request.createResponse();
        errorResponse.getHeaders().set("error", throwable.getMessage());
        errorResponse.getHeaders().set("status", "500");

        return errorResponse;
    }

    // ========== 生命周期方法 ==========

    @Override
    public void init() {
        if (running) {
            return;
        }

        log.info("[GatewayProcessor] 开始初始化网关处理器");

        // 初始化配置
        config.validate();

        // 初始化各个组件
        connectionPoolManager.init();
        routeManager.init();
        filterManager.init();
        loadBalanceManager.init();
        nodeManager.init();
        protocolConverterManager.init();

        log.info("[GatewayProcessor] 网关处理器初始化完成");
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        init();

        // 启动各个组件
        connectionPoolManager.start();
        routeManager.start();
        filterManager.start();
        loadBalanceManager.start();
        nodeManager.start();
        protocolConverterManager.start();

        running = true;
        log.info("[GatewayProcessor] 网关处理器启动完成");
    }

    @Override
    public void shutdown() {
        if (!running) {
            return;
        }

        running = false;
        log.info("[GatewayProcessor] 开始关闭网关处理器");

        // 关闭各个组件
        protocolConverterManager.shutdown();
        nodeManager.shutdown();
        loadBalanceManager.shutdown();
        filterManager.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();

        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }

    // ========== 辅助方法 ==========

    /**
     * 创建默认路由（临时实现）
     */
    protected UniversalRoute createDefaultRoute() {
        // TODO: 实现真正的默认路由创建
        return null; // 简化实现
    }

    /**
     * 创建默认节点（临时实现）
     */
    protected UniversalServiceNode createDefaultNode() {
        // TODO: 实现真正的默认节点创建
        return null; // 简化实现
    }

    // ========== 内部结果类 ==========

    protected static class RouteMatchResult {
        private final UniversalRoute route;
        private final Message request;

        public RouteMatchResult(UniversalRoute route, Message request) {
            this.route = route;
            this.request = request;
        }

        public UniversalRoute getRoute() {
            return route;
        }

        public Message getRequest() {
            return request;
        }
    }

    protected static class NodeSelectionResult {
        private final UniversalServiceNode node;
        private final Message request;

        public NodeSelectionResult(UniversalServiceNode node, Message request) {
            this.node = node;
            this.request = request;
        }

        public UniversalServiceNode getNode() {
            return node;
        }

        public Message getRequest() {
            return request;
        }
    }

    protected static class ConnectionAcquisitionResult {
        private final ClientConnection connection;
        private final Message request;

        public ConnectionAcquisitionResult(ClientConnection connection, Message request) {
            this.connection = connection;
            this.request = request;
        }

        public ClientConnection getConnection() {
            return connection;
        }

        public Message getRequest() {
            return request;
        }
    }
} 