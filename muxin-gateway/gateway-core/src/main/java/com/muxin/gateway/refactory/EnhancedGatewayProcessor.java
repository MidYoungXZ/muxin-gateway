package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.filter.*;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageMetadata;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.NodeManager;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.RouteTarget;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;
import com.muxin.gateway.refactory.connect.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强版网关处理器实现
 * 整合所有网关组件，提供完整的请求处理流程
 * <p>
 * 处理流程：
 * 1. 请求前处理（验证、限流、认证）
 * 2. 路由匹配和断言验证
 * 3. 负载均衡和节点选择
 * 4. 连接池获取连接
 * 5. 后端服务调用
 * 6. 响应后处理
 * 7. 连接归还和清理
 *
 * @author muxin
 */
@Slf4j
public class EnhancedGatewayProcessor implements GatewayProcessor {

    // ========== 核心组件 ==========
    private final RouteManager routeManager;
    private final FilterManager filterManager;
    private final LoadBalanceManager loadBalanceManager;
    private final NodeManager nodeManager;

    // ========== 增强组件 ==========
    private final ConnectionPool connectionPool;
    private final GatewayMetrics gatewayMetrics;
    private final GatewayConfig gatewayConfig;

    // ========== 统计信息 ==========
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong errorCounter = new AtomicLong(0);

    public EnhancedGatewayProcessor(RouteManager routeManager,
                                    FilterManager filterManager,
                                    LoadBalanceManager loadBalanceManager,
                                    NodeManager nodeManager) {
        this(routeManager, filterManager, loadBalanceManager, nodeManager,
                new DefaultConnectionPool(ConnectionPoolConfig.defaultConfig()));
    }

    public EnhancedGatewayProcessor(RouteManager routeManager,
                                    FilterManager filterManager,
                                    LoadBalanceManager loadBalanceManager,
                                    NodeManager nodeManager,
                                    ConnectionPool connectionPool) {
        this.routeManager = routeManager;
        this.filterManager = filterManager;
        this.loadBalanceManager = loadBalanceManager;
        this.nodeManager = nodeManager;
        this.connectionPool = connectionPool;

        // 初始化增强组件
        this.gatewayMetrics = new GatewayMetrics();
        this.gatewayConfig = new GatewayConfig();

        log.info("增强版网关处理器初始化完成，连接池配置: {}",
                connectionPool.getConfig());
    }

    @Override
    public CompletableFuture<Void> processRequest(UniversalRequestContext context) {
        requestCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        context.setAttribute("traceId", traceId);
        context.setAttribute("startTime", startTime);

        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 开始处理请求 - TraceId: {}", traceId);

        try {
            // ===== 第1步：请求前处理 =====
            executePreProcessing(context);

            // ===== 第2步：路由匹配 =====
            UniversalRoute matchedRoute = executeRouteMatching(context);

            // ===== 第3步：前置过滤器链 =====
            executePreFilters(context, matchedRoute);

            // ===== 第4步：负载均衡和节点选择 =====
            EndpointAddress targetEndpoint = executeLoadBalancing(context, matchedRoute);

            // ===== 第5步：路由过滤器链 =====
            executeRouteFilters(context, matchedRoute);

            // ===== 第6步：后端服务调用（异步） =====
            return executeBackendInvocation(context, targetEndpoint)
                    .thenRun(() -> {
                        // ===== 第7步：记录成功指标 =====
                        recordSuccessMetrics(context, startTime);

                        successCounter.incrementAndGet();
                        log.info("[ENHANCED_GATEWAY_PROCESSOR] 请求处理成功 - TraceId: {}, Duration: {}ms",
                                traceId, System.currentTimeMillis() - startTime);
                    })
                    .exceptionally(ex -> {
                        // 处理异步异常
                        Exception exception = ex instanceof Exception ? (Exception) ex : new RuntimeException(ex);
                        handleRequestException(context, exception, startTime, traceId);
                        throw new RuntimeException(exception);
                    });

        } catch (Exception e) {
            // 处理同步阶段的异常
            handleRequestException(context, e, startTime, traceId);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> processResponse(UniversalRequestContext context) {
        return CompletableFuture.runAsync(() -> {
            String traceId = context.getAttribute("traceId", String.class);

            try {
                log.debug("[ENHANCED_GATEWAY_PROCESSOR] 开始处理响应 - TraceId: {}", traceId);

                // ===== 第1步：后置过滤器链 =====
                executePostFilters(context);

                // ===== 第2步：响应后处理 =====
                executePostProcessing(context);

                // 标记处理完成
                context.markComplete();

                log.debug("[ENHANCED_GATEWAY_PROCESSOR] 响应处理完成 - TraceId: {}", traceId);

            } catch (Exception e) {
                log.error("[ENHANCED_GATEWAY_PROCESSOR] 响应处理失败 - TraceId: {}, Error: {}", 
                        traceId, e.getMessage(), e);
                processError(context, e);
            }
        });
    }

    @Override
    public void processError(UniversalRequestContext context, Exception exception) {
        String traceId = context.getAttribute("traceId", String.class);
        Long startTime = context.getAttribute("startTime", Long.class);

        try {
            log.error("[ENHANCED_GATEWAY_PROCESSOR] 处理错误 - TraceId: {}, Error: {}", 
                    traceId, exception.getMessage(), exception);

            // 设置错误信息
            context.setError(exception);
            errorCounter.incrementAndGet();

            // ===== 第1步：错误分类 =====
            ErrorType errorType = classifyError(exception);
            context.setAttribute("errorType", errorType);

            // ===== 第2步：错误过滤器处理 =====
            executeErrorFilters(context, exception);

            // ===== 第3步：创建错误响应 =====
            createErrorResponse(context, exception, errorType);

            // ===== 第4步：记录错误指标 =====
            recordErrorMetrics(context, exception, startTime);

        } catch (Exception e) {
            log.error("[ENHANCED_GATEWAY_PROCESSOR] 错误处理失败 - TraceId: {}, Error: {}", 
                    traceId, e.getMessage(), e);
        } finally {
            // 确保上下文标记完成
            context.markComplete();
        }
    }

    // ========== 请求处理各阶段实现 ==========

    /**
     * 第1步：请求前处理
     */
    private void executePreProcessing(UniversalRequestContext context) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行请求前处理");

        // 验证请求基本格式
        validateRequest(context);

        // 提取请求信息
        extractRequestInfo(context);

        // 设置默认属性
        setDefaultAttributes(context);
    }

    /**
     * 第2步：路由匹配
     */
    private UniversalRoute executeRouteMatching(UniversalRequestContext context) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行路由匹配");

        UniversalRoute matchedRoute = routeManager.matchRoute(context);
        if (matchedRoute == null) {
            throw new RuntimeException("未找到匹配的路由");
        }

        context.setMatchedRoute(matchedRoute);
        context.setAttribute("routeId", matchedRoute.getId());

        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 路由匹配成功 - RouteId: {}", 
                matchedRoute.getId());

        return matchedRoute;
    }

    /**
     * 第3步：前置过滤器处理
     */
    private void executePreFilters(UniversalRequestContext context, UniversalRoute route) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行前置过滤器链");

        Protocol protocol = context.getInboundProtocol();
        if (protocol != null) {
            UniversalFilterChain preChain = filterManager.createFilterChain(protocol, FilterType.PRE);
            preChain.filter(context);
        }
    }

    /**
     * 第4步：负载均衡和节点选择
     */
    private EndpointAddress executeLoadBalancing(UniversalRequestContext context, UniversalRoute route) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行负载均衡");

        RouteTarget target = route.getTarget();
        if (target == null) {
            throw new RuntimeException("路由目标为空");
        }

        // 获取可用节点
        List<EndpointAddress> availableTargets = target.getTargetAddresses();
        if (availableTargets.isEmpty()) {
            throw new RuntimeException("没有可用的目标节点");
        }

        // 负载均衡选择
        EndpointAddress selectedTarget = loadBalanceManager.selectTarget(
                route.getId(), availableTargets, context);

        if (selectedTarget == null) {
            throw new RuntimeException("负载均衡未能选择目标节点");
        }

        context.setSelectedNode(selectedTarget);
        context.setAttribute("targetAddress", selectedTarget.toUri());

        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 负载均衡选择成功 - Target: {}", 
                selectedTarget.toUri());

        return selectedTarget;
    }

    /**
     * 第5步：路由过滤器处理
     */
    private void executeRouteFilters(UniversalRequestContext context, UniversalRoute route) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行路由过滤器链");

        // 执行路由特定的过滤器
        for (UniversalFilter filter : route.getFilters()) {
            try {
                if (filter.isEnabled() &&
                        filter.getSupportedProtocols().contains(context.getInboundProtocol())) {
                    filter.filter(context, null);
                }
            } catch (Exception e) {
                log.error("路由过滤器执行失败: {}, 错误: {}", 
                        filter.getName(), e.getMessage(), e);
                throw new RuntimeException("路由过滤器执行失败: " + filter.getName(), e);
            }
        }
    }

    /**
     * 第6步：后端服务调用
     */
    private CompletableFuture<Void> executeBackendInvocation(UniversalRequestContext context, EndpointAddress target) {
        log.debug("执行后端服务调用，目标地址: {}", target.toUri());

        // 1. 从连接池获取连接，使用路由配置的连接超时
        Protocol protocol = context.getInboundProtocol();
        UniversalRoute route = (UniversalRoute) context.getMatchedRoute();
        Duration connectionTimeout = route != null ? route.getConnectionTimeout() : 
                Duration.ofMillis(gatewayConfig.getDefaultTimeout());

        return connectionPool.getConnection(target, protocol)
                .orTimeout(connectionTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .thenCompose(connection -> {
                    log.debug("连接池获取连接成功: {}", connection.getConnectionId());

                    // 2. 异步执行后端调用
                    return performBackendCallWithConnection(context, target, connection)
                            .thenApply(response -> {
                                context.setOutboundMessage(response);
                                log.info("后端服务调用成功，连接: {}", connection.getConnectionId());
                                return null; // CompletableFuture<Void>
                            })
                            .whenComplete((result, ex) -> {
                                // 3. 确保连接在任何情况下都被归还
                                connectionPool.returnConnection(connection);
                                log.debug("连接已归还到连接池: {}", connection.getConnectionId());
                            });
                })
                .handle((result, ex) -> {
                    if (ex != null) {
                        log.error("后端服务调用失败，目标: {} - {}", target.toUri(), ex.getMessage());

                        // 更新错误计数
                        context.setAttribute("errorType", ErrorType.BACKEND_INVOCATION_FAILED);
                        
                        // 设置错误响应到上下文
                        Message errorResponse = handleBackendError(context, target, ex);
                        context.setOutboundMessage(errorResponse);
                    }
                    return null; // 始终返回null表示CompletableFuture<Void>
                });
    }

    /**
     * 第1步（响应）：后置过滤器处理
     */
    private void executePostFilters(UniversalRequestContext context) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行后置过滤器链");

        Protocol protocol = context.getInboundProtocol();
        if (protocol != null) {
            UniversalFilterChain postChain = filterManager.createFilterChain(protocol, FilterType.POST);
            postChain.filter(context);
        }
    }

    /**
     * 第2步（响应）：响应后处理
     */
    private void executePostProcessing(UniversalRequestContext context) {
        log.debug("[ENHANCED_GATEWAY_PROCESSOR] 执行响应后处理");

        // 设置响应头
        setResponseHeaders(context);
    }

    // ========== 辅助方法实现 ==========

    /**
     * 验证请求
     */
    private void validateRequest(UniversalRequestContext context) {
        Message inboundMessage = context.getInboundMessage();
        if (inboundMessage == null) {
            throw new RuntimeException("入站消息为空");
        }

        if (inboundMessage.getProtocol() == null) {
            throw new RuntimeException("请求协议未指定");
        }
    }

    /**
     * 提取请求信息
     */
    private void extractRequestInfo(UniversalRequestContext context) {
        Message message = context.getInboundMessage();
        MessageMetadata metadata = message.getMetadata();

        if (metadata != null) {
            // 通过属性获取协议相关信息
            context.setAttribute("requestPath", metadata.getAttribute("path", String.class));
            context.setAttribute("requestMethod", metadata.getAttribute("method", String.class));
            context.setAttribute("sourceAddress", metadata.getSourceAddress());
            context.setAttribute("userAgent", message.getHeaders().get("User-Agent", String.class));
        }
    }

    /**
     * 设置默认属性
     */
    private void setDefaultAttributes(UniversalRequestContext context) {
        context.setAttribute("requestStartTime", LocalDateTime.now());
        context.setAttribute("timeout", gatewayConfig.getDefaultTimeout());
        context.setAttribute("maxRetries", gatewayConfig.getDefaultMaxRetries());
    }

    /**
     * 异步处理连接清理和日志记录
     */
    private void handleConnectionCleanup(Connection connection, Throwable ex, 
                                       UniversalRequestContext context, EndpointAddress target) {
        try {
            if (ex == null) {
                // 成功情况下的日志记录
                log.debug("后端调用响应接收成功，连接: {}, 耗时: {}ms", 
                         connection.getConnectionId(), 
                         System.currentTimeMillis() - context.getStartTime());
            } else {
                // 异常情况下的连接处理
                if (ex instanceof java.util.concurrent.TimeoutException) {
                    log.warn("连接超时: {}", connection.getConnectionId());
                    // 连接可能需要重置或标记，具体实现依赖Connection接口
                } else {
                    log.warn("连接发生异常: {} - {}", connection.getConnectionId(), ex.getMessage());
                }
            }
        } catch (Exception cleanupException) {
            log.error("连接清理过程中发生异常", cleanupException);
        }
    }

    /**
     * 异步处理后端调用错误
     */
    private Message handleBackendError(UniversalRequestContext context, EndpointAddress target, Throwable ex) {
        // 解包 CompletionException
        Throwable cause = ex instanceof java.util.concurrent.CompletionException ? ex.getCause() : ex;
        
        if (cause instanceof java.util.concurrent.TimeoutException) {
            UniversalRoute route = (UniversalRoute) context.getMatchedRoute();
            Duration timeout = route != null ? route.getRequestTimeout() : 
                    Duration.ofMillis(gatewayConfig.getDefaultTimeout());
            log.error("后端调用超时: {}, 超时配置: {}, 路由: {}", 
                     target.toUri(), timeout, route != null ? route.getId() : "unknown");
            
            // 创建超时错误响应
            return createTimeoutErrorResponse(context, target, timeout);
        } else {
            log.error("后端调用执行失败: {} - {}", target.toUri(), cause.getMessage());
            
            // 创建通用错误响应
            return createGenericErrorResponse(context, target, cause);
        }
    }

    /**
     * 创建超时错误响应
     */
    private Message createTimeoutErrorResponse(UniversalRequestContext context, EndpointAddress target, Duration timeout) {
        // 基于原始请求创建错误响应
        Message request = context.getInboundMessage();
        Message response = request.createResponse();
        
        // 设置错误信息到响应头中
        response.getHeaders().set("Error-Type", "REQUEST_TIMEOUT");
        response.getHeaders().set("Error-Message", "后端服务响应超时");
        response.getHeaders().set("Error-Target", target.toUri());
        response.getHeaders().set("Error-Timeout", timeout.toString());
        response.getHeaders().set("Error-Timestamp", String.valueOf(System.currentTimeMillis()));
        response.getHeaders().set("Status-Code", "504"); // Gateway Timeout
        
        return response;
    }

    /**
     * 创建通用错误响应
     */
    private Message createGenericErrorResponse(UniversalRequestContext context, EndpointAddress target, Throwable cause) {
        // 基于原始请求创建错误响应
        Message request = context.getInboundMessage();
        Message response = request.createResponse();
        
        // 设置错误信息到响应头中
        response.getHeaders().set("Error-Type", "BACKEND_ERROR");
        response.getHeaders().set("Error-Message", "后端服务调用失败");
        response.getHeaders().set("Error-Target", target.toUri());
        response.getHeaders().set("Error-Cause", cause.getMessage());
        response.getHeaders().set("Error-Timestamp", String.valueOf(System.currentTimeMillis()));
        response.getHeaders().set("Status-Code", "502"); // Bad Gateway
        
        return response;
    }

    /**
     * 执行实际的后端调用（使用连接池连接）
     */
    private CompletableFuture<Message> performBackendCallWithConnection(UniversalRequestContext context,
                                                                        EndpointAddress target,
                                                                        Connection connection) {
        Message request = context.getInboundMessage();

        // 从路由获取超时配置
        UniversalRoute route = (UniversalRoute) context.getMatchedRoute();
        Duration requestTimeout = route != null ? route.getRequestTimeout() : 
                Duration.ofMillis(gatewayConfig.getDefaultTimeout());

        log.debug("异步发送请求: {} -> {}, 请求超时: {}", 
                 connection.getConnectionId(), target.toUri(), requestTimeout);

        // 使用连接发送消息并异步处理响应
        return connection.send(request)
                .orTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((response, ex) -> {
                    // 异步处理连接清理和日志记录
                    handleConnectionCleanup(connection, ex, context, target);
                })
                .exceptionally(ex -> {
                    // 异步错误处理
                    return handleBackendError(context, target, ex);
                });
    }

    /**
     * 执行实际的后端调用（向后兼容方法）
     */
    private Message performBackendCall(UniversalRequestContext context, EndpointAddress target) {
        try {
            Message request = context.getInboundMessage();

            // 模拟后端调用延迟
            Thread.sleep(10);

            // 模拟创建响应
            Message response = request.createResponse();

            return response;

        } catch (Exception e) {
            throw new RuntimeException("后端调用执行失败", e);
        }
    }

    /**
     * 错误分类
     */
    private ErrorType classifyError(Exception exception) {
        if (exception instanceof RuntimeException) {
            return ErrorType.INTERNAL_ERROR;
        } else if (exception instanceof CompletionException) {
            return ErrorType.INTERNAL_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }

    /**
     * 执行错误过滤器
     */
    private void executeErrorFilters(UniversalRequestContext context, Exception exception) {
        try {
            Protocol protocol = context.getInboundProtocol();
            if (protocol != null) {
                UniversalFilterChain errorChain = filterManager.createFilterChain(protocol, FilterType.ERROR);
                errorChain.filter(context);
            }
        } catch (Exception e) {
            System.err.println("[ENHANCED_GATEWAY_PROCESSOR] 错误过滤器执行失败: " + e.getMessage());
        }
    }

    /**
     * 创建错误响应
     */
    private void createErrorResponse(UniversalRequestContext context, Exception exception, ErrorType errorType) {
        try {
            // 创建错误响应消息
            Message errorResponse = context.getInboundMessage().createResponse();
            context.setOutboundMessage(errorResponse);
        } catch (Exception e) {
            System.err.println("[ENHANCED_GATEWAY_PROCESSOR] 创建错误响应失败: " + e.getMessage());
        }
    }

    /**
     * 记录成功指标
     */
    private void recordSuccessMetrics(UniversalRequestContext context, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        String routeId = context.getAttribute("routeId", String.class);
        String targetAddress = context.getAttribute("targetAddress", String.class);

        gatewayMetrics.recordSuccess(routeId, targetAddress, duration);
    }

    /**
     * 记录错误指标
     */
    private void recordErrorMetrics(UniversalRequestContext context, Exception exception, Long startTime) {
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String routeId = context.getAttribute("routeId", String.class);
            String targetAddress = context.getAttribute("targetAddress", String.class);

            gatewayMetrics.recordError(routeId, targetAddress, duration, exception);
        }
    }

    /**
     * 处理请求异常
     */
    private void handleRequestException(UniversalRequestContext context, Exception e, long startTime, String traceId) {
        errorCounter.incrementAndGet();
        recordErrorMetrics(context, e, startTime);

        System.err.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 请求处理异常 - TraceId: %s, Duration: %dms, Error: %s",
                traceId, System.currentTimeMillis() - startTime, e.getMessage()));

        // 设置错误信息到上下文
        context.setError(e);
    }

    /**
     * 生成跟踪ID
     */
    private String generateTraceId() {
        return "trace-" + System.currentTimeMillis() + "-" +
                Long.toHexString(System.nanoTime()).substring(8);
    }

    /**
     * 设置响应头
     */
    private void setResponseHeaders(UniversalRequestContext context) {
        Message response = context.getOutboundMessage();
        if (response != null) {
            // 设置通用响应头
            response.getHeaders().set("X-Gateway-Version", "1.0");
            response.getHeaders().set("X-Trace-Id", context.getAttribute("traceId", String.class));
            response.getHeaders().set("X-Response-Time",
                    String.valueOf(System.currentTimeMillis() - context.getStartTime()));
        }
    }

    // ========== Getter方法 ==========

    @Override
    public RouteManager getRouteManager() {
        return routeManager;
    }

    @Override
    public FilterManager getFilterManager() {
        return filterManager;
    }

    @Override
    public LoadBalanceManager getLoadBalanceManager() {
        return loadBalanceManager;
    }

    @Override
    public NodeManager getNodeManager() {
        return nodeManager;
    }

    /**
     * 获取连接池
     */
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * 获取网关指标
     */
    public GatewayMetrics getGatewayMetrics() {
        return gatewayMetrics;
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", requestCounter.get());
        stats.put("successRequests", successCounter.get());
        stats.put("errorRequests", errorCounter.get());
        stats.put("successRate", calculateSuccessRate());
        return stats;
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = requestCounter.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successCounter.get() / total * 100.0;
    }

    // ========== 内部类定义 ==========

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        ROUTE_NOT_FOUND,
        TARGET_NOT_FOUND,
        NO_AVAILABLE_TARGETS,
        LOAD_BALANCE_FAILED,
        FILTER_EXECUTION_FAILED,
        BACKEND_INVOCATION_FAILED,
        CONNECTION_POOL_EXHAUSTED,
        RETRY_EXHAUSTED,
        CIRCUIT_BREAKER_OPEN,
        INTERNAL_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * 网关配置类
     */
    public static class GatewayConfig {
        private final int defaultTimeout = 30000;  // 30秒
        private final int defaultMaxRetries = 3;   // 3次重试

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public int getDefaultMaxRetries() {
            return defaultMaxRetries;
        }
    }

    /**
     * 网关指标类
     */
    public static class GatewayMetrics {
        private final long startTime = System.currentTimeMillis();
        private final Map<String, Long> successCounts = new HashMap<>();
        private final Map<String, Long> errorCounts = new HashMap<>();
        private final Map<String, Long> totalDurations = new HashMap<>();

        public void recordSuccess(String routeId, String targetAddress, long duration) {
            String key = routeId + "@" + targetAddress;
            successCounts.merge(key, 1L, Long::sum);
            totalDurations.merge(key, duration, Long::sum);
        }

        public void recordError(String routeId, String targetAddress, long duration, Exception exception) {
            String key = routeId + "@" + targetAddress;
            errorCounts.merge(key, 1L, Long::sum);
        }

        public long getStartTime() {
            return startTime;
        }

        public Map<String, Long> getSuccessCounts() {
            return new HashMap<>(successCounts);
        }

        public Map<String, Long> getErrorCounts() {
            return new HashMap<>(errorCounts);
        }
    }
} 