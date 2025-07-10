package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.ProtocolConverter;
import com.muxin.gateway.core.plus.protocol.message.ProtocolConverterManager;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.node.NodeManager;
import com.muxin.gateway.core.plus.route.node.ServiceNode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网关处理器抽象类
 * 定义了网关处理请求的标准流程，使用模板方法模式
 * 重构后使用UniversalRequestContext作为核心参数传递
 *
 * @author muxin
 */
@Slf4j
public abstract class GatewayProcessor implements LifeCycle {

    // ========== 核心组件依赖 ==========
    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final NodeManager nodeManager;
    protected final ProtocolConverterManager protocolConverterManager;

    // ========== 线程池管理 ==========
    protected final ExecutorService businessExecutor;

    // ========== 状态管理 ==========
    protected volatile boolean running = false;

    protected GatewayProcessor(GatewayConfig config,
                               ConnectionPoolManager connectionPoolManager,
                               RouteManager routeManager,
                               NodeManager nodeManager,
                               ProtocolConverterManager protocolConverterManager) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.nodeManager = nodeManager;
        this.protocolConverterManager = protocolConverterManager;

        // 初始化业务线程池
        this.businessExecutor = Executors.newFixedThreadPool(
                16, // 默认业务线程池大小
                r -> {
                    Thread thread = new Thread(r, "gateway-business-" + System.nanoTime());
                    thread.setDaemon(false);
                    return thread;
                }
        );

        log.info("[GatewayProcessor] 网关处理器创建完成");
    }

    /**
     * 处理入站请求 - 核心处理方法（单次线程切换优化版）
     * 使用UniversalRequestContext传递所有请求信息
     * <p>
     * 线程模型：
     * 1. 同步执行阶段（当前线程）: 步骤1-6（CPU密集型操作）
     * 2. 异步执行阶段（业务线程池）: 步骤7-10（I/O密集型操作）
     * <p>
     * 性能优势：
     * - 减少90%线程切换开销（从10次降到1次）
     * - CPU操作连续执行，缓存友好
     * - 线程池压力显著降低
     */
    public final void processRequest(RequestContext context) {
        long startTime = System.currentTimeMillis();
        String requestId = context.requestId();

        log.debug("[GatewayProcessor] 开始处理请求: {}", requestId);

        try {
            // ========== 同步执行阶段：CPU密集型操作 ==========

            // 第1步：请求接收与验证
            log.debug("[GatewayProcessor] 步骤1：请求接收与验证 - {}", requestId);
            validateRequest(context);

            // 第2步：协议转换（入站）
            log.debug("[GatewayProcessor] 步骤2：协议转换（入站）- {}", requestId);
            convertInboundProtocol(context);

            // 第3步：路由匹配
            log.debug("[GatewayProcessor] 步骤3：路由匹配 - {}", requestId);
            Route matchedRoute = matchRouteSync(context);
            context.setMatchedRoute(matchedRoute);

            // 第4步：过滤器处理（前置）
            log.debug("[GatewayProcessor] 步骤4：过滤器处理（前置）- {}", requestId);
            executePreFilters(context);

            // 第5步：负载均衡与节点选择
            log.debug("[GatewayProcessor] 步骤5：负载均衡与节点选择 - {}", requestId);
            ServiceNode selectedNode = selectTargetNodeSync(context);
            context.setSelectedNode(selectedNode);

            // 第6步：连接管理
            log.debug("[GatewayProcessor] 步骤6：连接管理 - {}", requestId);
            ClientConnection connection = acquireConnectionSync(context);
            context.setClientConnection(connection);

            // ========== 唯一的线程切换点：从同步切换到异步 ==========

            CompletableFuture.supplyAsync(() -> {
                        try {
                            // ========== 异步执行阶段：I/O密集型操作 ==========

                            // 第7步：后端服务调用
                            log.debug("[GatewayProcessor] 步骤7：后端服务调用 - {}", requestId);
                            Message response = invokeBackendServiceSync(context);
                            context.setOutboundMessage(response);

                            // 第8步：协议转换（出站）
                            log.debug("[GatewayProcessor] 步骤8：协议转换（出站）- {}", requestId);
                            convertOutboundProtocolSync(context);

                            // 第9步：过滤器处理（后置）
                            log.debug("[GatewayProcessor] 步骤9：过滤器处理（后置）- {}", requestId);
                            executePostFilters(context);

                            // 第10步：响应返回
                            log.debug("[GatewayProcessor] 步骤10：响应返回 - {}", requestId);
                            Message result = sendResponseSync(context);

                            long duration = System.currentTimeMillis() - startTime;
                            log.info("[GatewayProcessor] 请求处理完成: {} - 耗时: {}ms", requestId, duration);

                            return result;

                        } catch (Exception e) {
                            long duration = System.currentTimeMillis() - startTime;
                            log.error("[GatewayProcessor] 异步阶段处理失败: {} - 耗时: {}ms", requestId, duration, e);

                            // 异步阶段错误处理
                            Message errorResponse = handleError(context, e);
                            try {
                                sendResponseSync(context);
                            } catch (Exception sendError) {
                                log.error("[GatewayProcessor] 发送错误响应失败: {}", requestId, sendError);
                            }
                            return errorResponse;
                        }
                    }, businessExecutor)
                    .whenComplete((result, throwable) -> {
                        try {
                            // 【高优先级】资源清理 - 防止内存泄漏
                            cleanupResources(context);

                            if (throwable != null) {
                                long duration = System.currentTimeMillis() - startTime;
                                log.error("[GatewayProcessor] 异步完成阶段异常: {} - 耗时: {}ms", requestId, duration, throwable);
                            }

                        } finally {
                            // 标记上下文完成
                            context.markComplete();

                            // 清理临时状态
                            clearTemporaryState(context);
                        }
                    });

        } catch (Exception e) {
            // 同步阶段错误处理
            long duration = System.currentTimeMillis() - startTime;
            log.error("[GatewayProcessor] 同步阶段处理失败: {} - 耗时: {}ms", requestId, duration, e);

            try {
                Message errorResponse = handleError(context, e);
                sendResponseSync(context);
            } catch (Exception sendError) {
                log.error("[GatewayProcessor] 发送同步错误响应失败: {}", requestId, sendError);
            } finally {
                // 同步阶段也需要清理资源
                cleanupResources(context);
                context.markComplete();
                clearTemporaryState(context);
            }
        }
    }

    // ========== 同步版本方法（单次线程切换优化）==========

    /**
     * 同步版本：协议转换（入站）
     */
    protected void convertInboundProtocol(RequestContext context) {
        try {
            if (needsInboundProtocolConversion(context)) {
                performInboundProtocolConversion(context);
                log.debug("[GatewayProcessor] 入站协议转换完成: {} -> {}",
                        context.getOrigialInboundProtocol().getName(), "UNIVERSAL");
            }
        } catch (Exception e) {
            log.error("[GatewayProcessor] 入站协议转换失败", e);
            throw new RuntimeException("入站协议转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：路由匹配
     */
    protected Route matchRouteSync(RequestContext context) {
        try {
            Route matchedRoute = routeManager.matchRoute(context);

            if (matchedRoute != null) {
                log.debug("[GatewayProcessor] 路由匹配成功: {} -> {}",
                        context.getAttribute("path", String.class), matchedRoute.getId());
                return matchedRoute;
            } else {
                log.warn("[GatewayProcessor] 未找到匹配的路由，路径: {}",
                        context.getAttribute("path", String.class));
                throw new RuntimeException("未找到匹配的路由");
            }
        } catch (Exception e) {
            log.error("[GatewayProcessor] 路由匹配失败", e);
            throw new RuntimeException("路由匹配失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：负载均衡与节点选择
     */
    protected ServiceNode selectTargetNodeSync(RequestContext context) {
        try {
            Route route = context.getMatchedRoute();
            if (route == null) {
                throw new RuntimeException("没有匹配的路由信息");
            }

            String serviceName = extractServiceName(route);
            List<ServiceNode> healthyNodes = nodeManager.getHealthyNodes(serviceName);

            if (healthyNodes.isEmpty()) {
                log.error("[GatewayProcessor] 服务 {} 没有可用的健康节点", serviceName);
                throw new RuntimeException("服务不可用: " + serviceName);
            }

            List<EndpointAddress> availableTargets =
                    healthyNodes.stream()
                            .map(ServiceNode::getAddress)
                            .collect(java.util.stream.Collectors.toList());

            // 使用EnhancedRouteManager进行负载均衡（集成后的功能）
            EndpointAddress selectedAddress = null;
            if (routeManager instanceof com.muxin.gateway.core.plus.route.EnhancedRouteManager) {
                com.muxin.gateway.core.plus.route.EnhancedRouteManager enhancedManager =
                        (com.muxin.gateway.core.plus.route.EnhancedRouteManager) routeManager;
                selectedAddress = enhancedManager.selectTarget(route.getId(), availableTargets, context);
            } else {
                // 简单轮询负载均衡作为fallback
                selectedAddress = availableTargets.get((int) (System.nanoTime() % availableTargets.size()));
            }

            EndpointAddress finalSelectedAddress = selectedAddress;
            ServiceNode selectedNode = healthyNodes.stream()
                    .filter(node -> node.getAddress().equals(finalSelectedAddress))
                    .findFirst()
                    .orElse(null);

            if (selectedNode == null) {
                log.error("[GatewayProcessor] 负载均衡器未能选择节点，服务: {}", serviceName);
                throw new RuntimeException("负载均衡失败");
            }

            log.debug("[GatewayProcessor] 负载均衡选择节点: {} -> {}",
                    serviceName, selectedNode.getAddress().toUri());

            return selectedNode;

        } catch (Exception e) {
            log.error("[GatewayProcessor] 负载均衡失败", e);
            throw new RuntimeException("负载均衡失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：连接管理
     */
    protected ClientConnection acquireConnectionSync(RequestContext context) {
        ServiceNode node = (ServiceNode) context.getSelectedNode();
        if (node == null) {
            throw new RuntimeException("没有选定的服务节点");
        }

        try {
            // 使用正确的方法名和协议参数
            ClientConnection connection = connectionPoolManager
                    .getClientConnection(node.getAddress(), context.getOrigialInboundProtocol())
                    .get(); // 同步获取

            if (connection == null) {
                throw new RuntimeException("无法获取连接到: " + node.getAddress().toUri());
            }

            log.debug("[GatewayProcessor] 连接获取成功: {}", node.getAddress().toUri());
            return connection;

        } catch (Exception e) {
            log.error("[GatewayProcessor] 连接获取失败: {}", node.getAddress().toUri(), e);
            throw new RuntimeException("连接获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：后端服务调用
     */
    protected Message invokeBackendServiceSync(RequestContext context) {
        try {
            // 调用抽象方法，由子类实现具体的后端调用逻辑
            return invokeBackendService(context).get();
        } catch (Exception e) {
            log.error("[GatewayProcessor] 后端服务调用失败", e);
            throw new RuntimeException("后端服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：协议转换（出站）
     */
    protected void convertOutboundProtocolSync(RequestContext context) {
        try {
            if (needsOutboundProtocolConversion(context)) {
                performOutboundProtocolConversion(context);
                log.debug("[GatewayProcessor] 出站协议转换完成");
            }
        } catch (Exception e) {
            log.error("[GatewayProcessor] 出站协议转换失败", e);
            throw new RuntimeException("出站协议转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：响应返回
     */
    protected Message sendResponseSync(RequestContext context) {
        try {
            return sendResponse(context).get();
        } catch (Exception e) {
            log.error("[GatewayProcessor] 响应发送失败", e);
            throw new RuntimeException("响应发送失败: " + e.getMessage(), e);
        }
    }

    // ========== 模板方法定义（子类可以覆盖）==========

    /**
     * 第1步：请求接收与验证
     */
    protected void validateRequest(RequestContext context) {
        // 基本验证
        if (context.getOriginalInboundData() == null) {
            throw new IllegalArgumentException("入站原始数据不能为空");
        }
        log.debug("[GatewayProcessor] 请求验证与信息提取完成: {}", context.requestId());
    }
    /**
     * 检查是否需要入站协议转换
     */
    protected boolean needsInboundProtocolConversion(RequestContext context) {
        Protocol inboundProtocol = context.getOrigialInboundProtocol();

        // 如果已经是Universal协议，无需转换
        if (inboundProtocol instanceof Protocol.UniversalProtocol) {
            return false;
        }

        // 检查是否有协议转换器支持
        return protocolConverterManager.canConvert(inboundProtocol, Protocol.UNIVERSAL);
    }

    /**
     * 执行入站协议转换
     */
    protected void performInboundProtocolConversion(RequestContext context) {
        Protocol sourceProtocol = context.getOrigialInboundProtocol();
        Protocol targetProtocol = Protocol.UNIVERSAL;

        // 获取协议转换器并进行转换
        ProtocolConverter converter = protocolConverterManager.getConverter(sourceProtocol, targetProtocol);
        if (converter == null) {
            throw new RuntimeException("找不到协议转换器: " + sourceProtocol.getName() + " -> " + targetProtocol.getName());
        }

        Message convertedMessage = converter.convertToUniversal(context.getOriginalInboundData(), context);
        // 更新上下文中的消息
        context.setInboundMessage(convertedMessage);
    }


    /**
     * 第4步：过滤器处理（前置）
     */
    protected void executePreFilters(RequestContext context) {
        try {
            // 过滤器功能已集成到路由处理中，这里暂时跳过
            // TODO: 实现基于路由配置的过滤器执行
            log.debug("[GatewayProcessor] 前置过滤器执行完成（简化实现）");
        } catch (Exception e) {
            log.error("[GatewayProcessor] 前置过滤器执行失败", e);
            throw new RuntimeException("前置过滤器执行失败: " + e.getMessage(), e);
        }
    }


    /**
     * 第7步：后端服务调用（抽象方法，子类必须实现）
     */
    protected abstract CompletableFuture<Message> invokeBackendService(RequestContext context);

    /**
     * 第8步：协议转换（出站）
     */
    protected CompletableFuture<RequestContext> convertOutboundProtocol(RequestContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 【低优先级】协议转换增强 - 多协议支持时需要
                if (needsOutboundProtocolConversion(context)) {
                    performOutboundProtocolConversion(context);
                    log.debug("[GatewayProcessor] 出站协议转换完成: {} -> {}",
                            "UNIVERSAL", context.getOrigialInboundProtocol().getName());
                }
                return context;
            } catch (Exception e) {
                log.error("[GatewayProcessor] 出站协议转换失败", e);
                throw new RuntimeException("出站协议转换失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 检查是否需要出站协议转换
     */
    protected boolean needsOutboundProtocolConversion(RequestContext context) {
        Protocol outboundProtocol = context.getOutboundMessage().getProtocol();
        Protocol targetProtocol = context.getOrigialInboundProtocol();

        // 如果出站消息协议与入站协议相同，无需转换
        if (outboundProtocol.equals(targetProtocol)) {
            return false;
        }

        // 检查是否有协议转换器支持
        return protocolConverterManager.canConvert(outboundProtocol, targetProtocol);
    }

    /**
     * 执行出站协议转换
     */
    protected void performOutboundProtocolConversion(RequestContext context) {
        Protocol sourceProtocol = context.getOutboundMessage().getProtocol();
        Protocol targetProtocol = context.getOrigialInboundProtocol();

        // 获取协议转换器并进行转换
        ProtocolConverter converter = protocolConverterManager.getConverter(sourceProtocol, targetProtocol);
        if (converter == null) {
            throw new RuntimeException("找不到协议转换器: " + sourceProtocol.getName() + " -> " + targetProtocol.getName());
        }

        Object convertedResponse = converter.convertFromUniversal(context.getOutboundMessage(), context);

        // 这里需要将转换后的对象重新包装为Message（简化实现）
        // 实际项目中需要更复杂的逻辑来处理不同协议的响应
        log.debug("[GatewayProcessor] 出站协议转换完成，转换为: {}", convertedResponse.getClass().getSimpleName());
    }

    /**
     * 第9步：过滤器处理（后置）
     */
    protected void executePostFilters(RequestContext context) {
        try {
            // 过滤器功能已集成到路由处理中，这里暂时跳过
            // TODO: 实现基于路由配置的过滤器执行
            log.debug("[GatewayProcessor] 后置过滤器执行完成（简化实现）");
        } catch (Exception e) {
            log.error("[GatewayProcessor] 后置过滤器执行失败", e);
            throw new RuntimeException("后置过滤器执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 第10步：响应返回
     */
    protected CompletableFuture<Message> sendResponse(RequestContext context) {
        ServerConnection serverConnection = context.serverConnection();
        Message response = context.getOutboundMessage();

        return serverConnection.sendResponse(response).thenApply(v -> response);
    }

    /**
     * 异常处理
     */
    protected Message handleError(RequestContext context, Throwable throwable) {
        log.error("[GatewayProcessor] 处理错误", throwable);

        ServerConnection serverConnection = (ServerConnection) context.serverConnection();
        Message request = context.getInboundMessage();

        // 【中优先级】错误处理分类 - 提高用户体验
        ErrorType errorType = classifyError(throwable);
        ErrorInfo errorInfo = createErrorInfo(errorType, throwable, context);

        // 记录错误（用于监控，这里简化实现）
        recordError(errorType, errorInfo, context);

        // 发送错误响应
        serverConnection.sendError(throwable);

        // 创建分类后的错误响应消息
        Message errorResponse = createErrorResponse(request, errorInfo);

        // 设置错误到上下文
        context.setError(throwable);

        return errorResponse;
    }

    /**
     * 错误分类
     */
    protected ErrorType classifyError(Throwable throwable) {
        if (throwable == null) {
            return ErrorType.UNKNOWN;
        }

        String message = throwable.getMessage();
        String className = throwable.getClass().getSimpleName();

        // 超时错误
        if (throwable instanceof java.util.concurrent.TimeoutException ||
                throwable instanceof java.net.SocketTimeoutException ||
                (message != null && message.toLowerCase().contains("timeout"))) {
            return ErrorType.TIMEOUT;
        }

        // 连接错误
        if (throwable instanceof java.net.ConnectException ||
                throwable instanceof java.net.NoRouteToHostException ||
                throwable instanceof java.nio.channels.UnresolvedAddressException ||
                (message != null && (message.toLowerCase().contains("connection") ||
                        message.toLowerCase().contains("connect")))) {
            return ErrorType.CONNECTION_ERROR;
        }

        // 路由错误
        if (message != null && (message.toLowerCase().contains("路由") ||
                message.toLowerCase().contains("route"))) {
            return ErrorType.ROUTE_NOT_FOUND;
        }

        // 服务不可用
        if (message != null && (message.toLowerCase().contains("服务不可用") ||
                message.toLowerCase().contains("service unavailable") ||
                message.toLowerCase().contains("no healthy"))) {
            return ErrorType.SERVICE_UNAVAILABLE;
        }

        // 认证错误
        if (throwable instanceof SecurityException ||
                (message != null && (message.toLowerCase().contains("unauthorized") ||
                        message.toLowerCase().contains("authentication")))) {
            return ErrorType.AUTHENTICATION_ERROR;
        }

        // 协议转换错误
        if (className.contains("ProtocolConversion") ||
                (message != null && message.toLowerCase().contains("protocol"))) {
            return ErrorType.PROTOCOL_ERROR;
        }

        // 负载均衡错误
        if (message != null && message.toLowerCase().contains("负载均衡")) {
            return ErrorType.LOAD_BALANCE_ERROR;
        }

        // 默认为内部错误
        return ErrorType.INTERNAL_ERROR;
    }

    /**
     * 创建错误信息
     */
    protected ErrorInfo createErrorInfo(ErrorType errorType, Throwable throwable, RequestContext context) {
        return ErrorInfo.builder()
                .errorType(errorType)
                .errorCode(errorType.getCode())
                .errorMessage(getErrorMessage(errorType, throwable))
                .originalException(throwable)
                .timestamp(System.currentTimeMillis())
                .requestId(context.getInboundMessage().getMessageId())
                .build();
    }

    /**
     * 获取用户友好的错误消息
     */
    protected String getErrorMessage(ErrorType errorType, Throwable throwable) {
        String userMessage = errorType.getUserMessage();

        // 在开发环境可以包含更多技术细节（这里简化为总是包含技术细节）
        if (throwable.getMessage() != null && !throwable.getMessage().isEmpty()) {
            return userMessage + " (技术细节: " + throwable.getMessage() + ")";
        }

        return userMessage;
    }

    /**
     * 记录错误信息（用于监控和统计）
     */
    protected void recordError(ErrorType errorType, ErrorInfo errorInfo, RequestContext context) {
        // 这里可以集成监控系统，记录错误指标
        // 由于监控埋点不在本次实现范围内，这里只做日志记录
        log.warn("[GatewayProcessor] 错误分类记录 - 类型: {}, 代码: {}, 消息: {}, 请求ID: {}",
                errorType.name(), errorInfo.getErrorCode(), errorInfo.getErrorMessage(), errorInfo.getRequestId());
    }

    /**
     * 创建错误响应消息
     */
    protected Message createErrorResponse(Message request, ErrorInfo errorInfo) {
        Message errorResponse = request.createResponse();

        // 设置错误相关的头部信息
        errorResponse.getHeaders().set("X-Error-Type", errorInfo.getErrorType().name());
        errorResponse.getHeaders().set("X-Error-Code", errorInfo.getErrorCode());
        errorResponse.getHeaders().set("X-Request-Id", errorInfo.getRequestId());
        errorResponse.getHeaders().set("X-Timestamp", String.valueOf(errorInfo.getTimestamp()));

        // 设置HTTP状态码（如果是HTTP协议）
        if (request.getProtocol() instanceof Protocol.HttpProtocol) {
            errorResponse.getHeaders().set("status", String.valueOf(errorInfo.getErrorType().getHttpStatus()));
        }

        // 设置错误响应体（JSON格式）
        String errorBody = String.format("""
                        {
                            "error": {
                                "type": "%s",
                                "code": "%s",
                                "message": "%s",
                                "timestamp": %d,
                                "requestId": "%s"
                            }
                        }
                        """,
                errorInfo.getErrorType().name(),
                errorInfo.getErrorCode(),
                errorInfo.getErrorMessage(),
                errorInfo.getTimestamp(),
                errorInfo.getRequestId()
        );

        // 更新响应体（这里需要根据实际的MessageBody实现来设置）
        try {
            if (errorResponse.getBody() != null) {
                // 简化实现：直接打印到日志，实际应该设置到Body中
                log.debug("[GatewayProcessor] 错误响应体: {}", errorBody);
            }
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 设置错误响应体失败", e);
        }

        return errorResponse;
    }

    // ========== 错误类型定义 ==========

    /**
     * 错误类型枚举
     */
    protected enum ErrorType {
        TIMEOUT("E001", "请求超时，请稍后重试", 504),
        CONNECTION_ERROR("E002", "连接失败，请检查网络", 502),
        ROUTE_NOT_FOUND("E003", "找不到指定的服务路径", 404),
        SERVICE_UNAVAILABLE("E004", "服务暂时不可用，请稍后重试", 503),
        AUTHENTICATION_ERROR("E005", "认证失败，请检查权限", 401),
        PROTOCOL_ERROR("E006", "协议转换失败", 400),
        LOAD_BALANCE_ERROR("E007", "负载均衡失败", 502),
        INTERNAL_ERROR("E500", "系统内部错误", 500),
        UNKNOWN("E999", "未知错误", 500);

        private final String code;
        private final String userMessage;
        private final int httpStatus;

        ErrorType(String code, String userMessage, int httpStatus) {
            this.code = code;
            this.userMessage = userMessage;
            this.httpStatus = httpStatus;
        }

        public String getCode() {
            return code;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public int getHttpStatus() {
            return httpStatus;
        }
    }

    /**
     * 错误信息封装类
     */
    protected static class ErrorInfo {
        private final ErrorType errorType;
        private final String errorCode;
        private final String errorMessage;
        private final Throwable originalException;
        private final long timestamp;
        private final String requestId;

        private ErrorInfo(Builder builder) {
            this.errorType = builder.errorType;
            this.errorCode = builder.errorCode;
            this.errorMessage = builder.errorMessage;
            this.originalException = builder.originalException;
            this.timestamp = builder.timestamp;
            this.requestId = builder.requestId;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public ErrorType getErrorType() {
            return errorType;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Throwable getOriginalException() {
            return originalException;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getRequestId() {
            return requestId;
        }

        public static class Builder {
            private ErrorType errorType;
            private String errorCode;
            private String errorMessage;
            private Throwable originalException;
            private long timestamp;
            private String requestId;

            public Builder errorType(ErrorType errorType) {
                this.errorType = errorType;
                return this;
            }

            public Builder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }

            public Builder originalException(Throwable originalException) {
                this.originalException = originalException;
                return this;
            }

            public Builder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder requestId(String requestId) {
                this.requestId = requestId;
                return this;
            }

            public ErrorInfo build() {
                return new ErrorInfo(this);
            }
        }
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
        // filterManager和loadBalanceManager已移除，功能集成到RouteManager中
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
        // filterManager和loadBalanceManager已移除，功能集成到RouteManager中
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
        // filterManager和loadBalanceManager已移除，功能集成到RouteManager中
        routeManager.shutdown();
        connectionPoolManager.shutdown();

        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }

    // ========== 资源清理方法 ==========

    /**
     * 【高优先级】资源清理 - 防止内存泄漏
     */
    protected void cleanupResources(RequestContext context) {
        try {
            // 1. 清理出站连接
            cleanupOutboundConnection(context);

            // 2. 清理连接池资源
            cleanupConnectionPoolResources(context);

            // 3. 清理协议特定资源
            cleanupProtocolResources(context);

            // 4. 清理过滤器资源
            cleanupFilterResources(context);

            log.debug("[GatewayProcessor] 资源清理完成 - 请求ID: {}",
                    context.getInboundMessage().getMessageId());

        } catch (Exception e) {
            log.warn("[GatewayProcessor] 资源清理过程中发生异常", e);
        }
    }

    /**
     * 清理出站连接
     */
    protected void cleanupOutboundConnection(RequestContext context) {
        try {
            if (context.clientConnection() != null) {
                ClientConnection outboundConnection = context.clientConnection();

                // 归还连接到池中（如果可复用）
                if (outboundConnection.isActive() && !context.hasError()) {
                    outboundConnection.returnToPool();
                    log.debug("[GatewayProcessor] 出站连接已归还到连接池: {}",
                            outboundConnection.getConnectionId());
                } else {
                    // 连接有问题，直接关闭
                    outboundConnection.close().whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.warn("[GatewayProcessor] 关闭出站连接失败: {}",
                                    outboundConnection.getConnectionId(), throwable);
                        } else {
                            log.debug("[GatewayProcessor] 出站连接已关闭: {}",
                                    outboundConnection.getConnectionId());
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理出站连接异常", e);
        }
    }

    /**
     * 清理连接池资源
     */
    protected void cleanupConnectionPoolResources(RequestContext context) {
        try {
            // 通知连接池管理器进行清理（如果有必要）
            if (context.hasError()) {
                // 错误情况下可能需要特殊处理
                connectionPoolManager.cleanupIdleConnections();
            }
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理连接池资源异常", e);
        }
    }

    /**
     * 清理协议特定资源
     */
    protected void cleanupProtocolResources(RequestContext context) {
        try {
            // 清理协议转换器相关资源
            // 这里可以根据协议类型进行特定的清理
            Message inboundMessage = context.getInboundMessage();
            Message outboundMessage = context.getOutboundMessage();

            if (inboundMessage != null && inboundMessage.getProtocol() != null) {
                // HTTP协议特定清理
                if (inboundMessage.getProtocol() instanceof Protocol.HttpProtocol) {
                    cleanupHttpResources(context);
                }
            }
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理协议特定资源异常", e);
        }
    }

    /**
     * 清理HTTP协议特定资源
     */
    protected void cleanupHttpResources(RequestContext context) {
        try {
            // HTTP特定的资源清理
            // 例如：清理大文件上传的临时文件、清理流等
            log.debug("[GatewayProcessor] HTTP协议资源清理完成");
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理HTTP资源异常", e);
        }
    }

    /**
     * 清理过滤器资源
     */
    protected void cleanupFilterResources(RequestContext context) {
        try {
            // 过滤器可能会持有一些资源需要清理
            // 例如：缓存、临时文件、数据库连接等
            log.debug("[GatewayProcessor] 过滤器资源清理完成");
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理过滤器资源异常", e);
        }
    }

    /**
     * 清理临时状态
     */
    protected void clearTemporaryState(RequestContext context) {
        try {
            // 1. 清理上下文中的临时属性
            clearTemporaryAttributes(context);

            // 2. 清理引用，帮助GC
            clearObjectReferences(context);

            log.debug("[GatewayProcessor] 临时状态清理完成 - 请求ID: {}",
                    context.getInboundMessage().getMessageId());

        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理临时状态异常", e);
        }
    }

    /**
     * 清理临时属性
     */
    protected void clearTemporaryAttributes(RequestContext context) {
        try {
            // 清理一些临时属性，保留重要的属性用于日志和监控
            String[] temporaryKeys = {
                    "nettyRequest", "nettyContext", "tempFile", "uploadBuffer",
                    "processingStart", "routeCache", "filterCache"
            };

            for (String key : temporaryKeys) {
                context.setAttribute(key, null);
            }

            log.debug("[GatewayProcessor] 临时属性清理完成");
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理临时属性异常", e);
        }
    }

    /**
     * 清理对象引用，帮助GC
     */
    protected void clearObjectReferences(RequestContext context) {
        try {
            // 这里可以清理一些不再需要的对象引用
            // 注意：不要清理还需要的引用，如inboundMessage等

            // 清理出站连接引用（已经处理过了）
            context.setClientConnection(null);

            log.debug("[GatewayProcessor] 对象引用清理完成");
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 清理对象引用异常", e);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 从路由中提取服务名（支持多种提取策略）
     */
    protected String extractServiceName(Route route) {
        if (route == null) {
            return "default-service";
        }

        // 策略1：从路由元数据中获取明确的服务名
        String serviceName = extractServiceNameFromMetadata(route);
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName;
        }

        // 策略2：从路由ID中解析服务名（支持多种格式）
        serviceName = extractServiceNameFromRouteId(route.getId());
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName;
        }

        // 策略3：从路由目标中推断服务名
        serviceName = extractServiceNameFromTarget(route);
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName;
        }

        // 策略4：使用路由名称作为服务名
        if (route.getName() != null && !route.getName().isEmpty()) {
            return normalizeServiceName(route.getName());
        }

        // 最后的兜底策略：使用路由ID或默认值
        return route.getId() != null ? normalizeServiceName(route.getId()) : "default-service";
    }

    /**
     * 从路由元数据中提取服务名
     */
    private String extractServiceNameFromMetadata(Route route) {
        if (route.getMetadata() == null) {
            return null;
        }

        // 尝试多个可能的元数据键
        String[] possibleKeys = {
                "serviceName", "service_name", "service",
                "targetService", "target_service", "destination"
        };

        for (String key : possibleKeys) {
            Object value = route.getMetadata().get(key);
            if (value instanceof String && !((String) value).isEmpty()) {
                return normalizeServiceName((String) value);
            }
        }

        return null;
    }

    /**
     * 从路由ID中解析服务名（支持多种命名格式）
     */
    private String extractServiceNameFromRouteId(String routeId) {
        if (routeId == null || routeId.isEmpty()) {
            return null;
        }

        // 格式1：serviceName-xxx (例如: user-service-v1)
        if (routeId.contains("-")) {
            String[] parts = routeId.split("-");
            if (parts.length >= 2) {
                // 如果是 user-service-v1 格式，返回 user-service
                if (parts.length >= 3 && "service".equals(parts[1])) {
                    return parts[0] + "-" + parts[1];
                }
                // 否则返回第一部分
                return parts[0];
            }
        }

        // 格式2：serviceName_xxx (例如: user_service_v1)
        if (routeId.contains("_")) {
            String[] parts = routeId.split("_");
            if (parts.length >= 2) {
                if (parts.length >= 3 && "service".equals(parts[1])) {
                    return (parts[0] + "_" + parts[1]).replace("_", "-");
                }
                return parts[0];
            }
        }

        // 格式3：service.name.xxx (例如: user.service.v1)
        if (routeId.contains(".")) {
            String[] parts = routeId.split("\\.");
            if (parts.length >= 2) {
                if (parts.length >= 3 && "service".equals(parts[1])) {
                    return (parts[0] + "-" + parts[1]);
                }
                return parts[0];
            }
        }

        // 格式4：驼峰命名转换 (例如: UserService -> user-service)
        if (Character.isUpperCase(routeId.charAt(0))) {
            return camelCaseToKebabCase(routeId);
        }

        // 其他情况直接返回规范化后的路由ID
        return normalizeServiceName(routeId);
    }

    /**
     * 从路由目标中推断服务名
     */
    private String extractServiceNameFromTarget(Route route) {
        try {
            if (route.getTarget() != null) {
                // 尝试从目标地址中提取服务名
                var addresses = route.getTarget().getTargetAddresses();
                if (addresses != null && !addresses.isEmpty()) {
                    var firstAddress = addresses.get(0);

                    // 从主机名中提取服务名 (例如: user-service.default.svc.cluster.local -> user-service)
                    String host = firstAddress.getHost();
                    if (host != null && !host.isEmpty()) {
                        // Kubernetes服务格式
                        if (host.contains(".")) {
                            String serviceName = host.split("\\.")[0];
                            if (!serviceName.isEmpty()) {
                                return normalizeServiceName(serviceName);
                            }
                        }

                        // 其他情况
                        return normalizeServiceName(host);
                    }
                }

                // 尝试从目标配置中获取
                var targetConfig = route.getTarget().getTargetConfig();
                if (targetConfig != null) {
                    Object serviceNameObj = targetConfig.get("serviceName");
                    if (serviceNameObj instanceof String) {
                        return normalizeServiceName((String) serviceNameObj);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[GatewayProcessor] 从路由目标中提取服务名失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 规范化服务名（转换为小写，使用连字符分隔）
     */
    private String normalizeServiceName(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return null;
        }

        return serviceName
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\-_]", "-") // 替换特殊字符为连字符
                .replaceAll("_+", "-")               // 下划线转连字符
                .replaceAll("-+", "-")               // 多个连字符合并为一个
                .replaceAll("^-+|-+$", "");          // 去除首尾连字符
    }

    /**
     * 驼峰命名转换为kebab-case (例如: UserService -> user-service)
     */
    private String camelCaseToKebabCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input
                .replaceAll("([a-z])([A-Z])", "$1-$2")  // 在小写字母后跟大写字母的地方插入连字符
                .toLowerCase();
    }
} 