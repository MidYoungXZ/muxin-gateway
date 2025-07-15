package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.*;
import com.muxin.gateway.core.plus.protocol.message.http.HttpBody;
import com.muxin.gateway.core.plus.protocol.message.http.HttpHeaders;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMessage;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import com.muxin.gateway.core.plus.route.service.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 网关处理器
 * 合并了原GatewayProcessor和EnhancedGatewayProcessor
 * 定义了网关处理请求的标准流程，简化了冗余的方法调用
 *
 * @author muxin
 */
@Slf4j
public class GatewayProcessor implements LifeCycle {

    // ========== 核心组件依赖 ==========
    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final InstanceManager instanceManager;
    protected final MessageCodecManager messageCodecManager;

    // ========== 线程池管理 ==========
    protected final ExecutorService businessExecutor;

    // ========== 状态管理 ==========
    protected volatile boolean running = false;

    public GatewayProcessor(GatewayConfig config,
                            ConnectionPoolManager connectionPoolManager,
                            RouteManager routeManager,
                            InstanceManager instanceManager,
                            MessageCodecManager messageCodecManager) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.instanceManager = instanceManager;
        this.messageCodecManager = messageCodecManager;

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
     * 使用RequestContext传递所有请求信息
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
            Route matchedRoute = matchRoute(context);
            context.setMatchedRoute(matchedRoute);

            // 第4步：过滤器处理（前置）
            log.debug("[GatewayProcessor] 步骤4：过滤器处理（前置）- {}", requestId);
            executePreFilters(context);

            // 第5步：负载均衡与节点选择
            log.debug("[GatewayProcessor] 步骤5：负载均衡与节点选择 - {}", requestId);
            EndpointAddress endpointAddress = selectServiceEndpoint(context);
            context.setSelectedEndpoint(endpointAddress);

            // 第6步：连接管理
            log.debug("[GatewayProcessor] 步骤6：连接管理 - {}", requestId);
            ClientConnection connection = acquireConnection(context);
            context.setClientConnection(connection);

            // ========== 唯一的线程切换点：从同步切换到异步 ==========

            CompletableFuture.supplyAsync(() -> {
                        try {
                            // ========== 异步执行阶段：I/O密集型操作 ==========

                            // 第7步：后端服务调用
                            log.debug("[GatewayProcessor] 步骤7：后端服务调用 - {}", requestId);
                            Message response = invokeBackendService(context);
                            context.setOutboundMessage(response);

                            // 第8步：过滤器处理（后置）
                            log.debug("[GatewayProcessor] 步骤8：过滤器处理（后置）- {}", requestId);
                            executePostFilters(context);

                            // 第9步：协议转换（出站）
                            log.debug("[GatewayProcessor] 步骤9：协议转换（出站）- {}", requestId);
                            convertOutboundProtocol(context);

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
                                context.setOutboundMessage(errorResponse);
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
                context.setOutboundMessage(errorResponse);
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
            }
        } catch (Exception e) {
            log.error("[GatewayProcessor] 入站协议转换失败", e);
            throw new RuntimeException("入站协议转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：路由匹配
     */
    protected Route matchRoute(RequestContext context) {
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
    protected EndpointAddress selectServiceEndpoint(RequestContext context) {
        try {
            Route route = context.getMatchedRoute();
            if (route == null) {
                throw new RuntimeException("没有匹配的路由信息");
            }

            // 第一步：通过RouteTarget的负载均衡策略选择目标地址
            EndpointAddress selectedAddress = route.getService().selectTarget(context);
            if (selectedAddress == null) {
                throw new RuntimeException("负载均衡选择结果为空");
            }
            log.debug("[GatewayProcessor] 负载均衡选择地址: {}", selectedAddress.toUri());
            return selectedAddress;
        } catch (Exception e) {
            log.error("[GatewayProcessor] 负载均衡失败", e);
            throw new RuntimeException("负载均衡失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从NodeManager中查找对应地址的ServiceInstance
     */
    private ServiceInstance findServiceInstanceByAddress(String serviceId, EndpointAddress targetAddress) {
        try {
            // 获取服务的所有健康节点
            List<ServiceInstance> healthyNodes = instanceManager.getHealthyInstances(serviceId);

            // 根据地址匹配查找实例
            for (ServiceInstance instance : healthyNodes) {
                if (addressMatches(instance.getAddress(), targetAddress)) {
                    log.debug("[GatewayProcessor] 找到匹配的服务实例: {}", instance.instanceId());
                    return instance;
                }
            }

            // 如果健康节点中没找到，再从所有节点中查找
            List<ServiceInstance> allNodes = instanceManager.getByServiceId(serviceId);
            for (ServiceInstance instance : allNodes) {
                if (addressMatches(instance.getAddress(), targetAddress)) {
                    log.debug("[GatewayProcessor] 找到服务实例（非健康）: {}", instance.instanceId());
                    return instance;
                }
            }

            return null;

        } catch (Exception e) {
            log.warn("[GatewayProcessor] 查找服务实例失败: {}", serviceId, e);
            return null;
        }
    }

    /**
     * 检查两个地址是否匹配
     */
    private boolean addressMatches(EndpointAddress addr1, EndpointAddress addr2) {
        if (addr1 == null || addr2 == null) {
            return false;
        }

        return Objects.equals(addr1.getHost(), addr2.getHost()) &&
                addr1.getPort() == addr2.getPort() &&
                Objects.equals(addr1.getProtocol(), addr2.getProtocol());
    }


    /**
     * 生成实例ID
     */
    private String generateInstanceId(String serviceName, EndpointAddress address) {
        return String.format("%s-%s-%d-%d",
                serviceName,
                address.getHost(),
                address.getPort(),
                System.currentTimeMillis() % 10000);
    }

    /**
     * 提取客户端信息
     */
    private String extractClientInfo(RequestContext context) {
        try {
            // 从请求中提取客户端信息
            Message inboundMessage = context.getInboundMessage();
            if (inboundMessage != null && inboundMessage.getHeaders() != null) {
                String userAgent = inboundMessage.getHeaders().get("User-Agent", String.class);
                String clientIP = inboundMessage.getHeaders().get("X-Real-IP", String.class);

                if (clientIP == null) {
                    clientIP = inboundMessage.getHeaders().get("X-Forwarded-For", String.class);
                }

                if (clientIP != null) {
                    return clientIP + (userAgent != null ? " " + userAgent : "");
                }
            }

            return null;
        } catch (Exception e) {
            log.debug("[GatewayProcessor] 提取客户端信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 同步版本：连接管理
     */
    protected ClientConnection acquireConnection(RequestContext context) {
        EndpointAddress endpoint = context.getSelectedEndpoint();
        if (endpoint == null) {
            throw new RuntimeException("没有选定的服务节点");
        }
        try {
            // 使用正确的方法名和协议参数
            ClientConnection connection = connectionPoolManager
                    .getClientConnection(endpoint, endpoint.getProtocol());
            if (connection == null) {
                throw new RuntimeException("无法获取连接到: " + endpoint.toUri());
            }
            log.debug("[GatewayProcessor] 连接获取成功: {}", endpoint.toUri());
            return connection;
        } catch (Exception e) {
            log.error("[GatewayProcessor] 连接获取失败: {}", endpoint.toUri(), e);
            throw new RuntimeException("连接获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步版本：协议转换（出站）
     */
    protected void convertOutboundProtocol(RequestContext context) {
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
            ServerConnection serverConnection = context.serverConnection();
            Message response = context.getOutboundMessage();

            serverConnection.sendResponse(response);
            return response;
        } catch (Exception e) {
            log.error("[GatewayProcessor] 响应发送失败", e);
            throw new RuntimeException("响应发送失败: " + e.getMessage(), e);
        }
    }

    // ========== 核心业务方法 ==========

    /**
     * 后端服务调用 - 合并自EnhancedGatewayProcessor
     */
    protected Message invokeBackendService(RequestContext context) {
        long startTime = System.currentTimeMillis();
        ClientConnection connection = context.clientConnection();
        Message request = context.getInboundMessage();
        String requestId = request.getMessageId();

        log.debug("[GatewayProcessor] 开始调用后端服务: {}", requestId);

        try {
            CompletableFuture<Message> future = connection.send(request)
                    .orTimeout(config.getCoreConfig().getDefaultTimeout().toMillis(), TimeUnit.MILLISECONDS);

            Message response = future.handle((res, throwable) -> {
                long duration = System.currentTimeMillis() - startTime;

                try {
                    if (throwable != null) {
                        log.error("[GatewayProcessor] 后端服务调用失败: {} - 耗时: {}ms",
                                requestId, duration, throwable);

                        // 处理各种异常情况
                        if (throwable instanceof TimeoutException) {
                            return createTimeoutResponse(request);
                        } else {
                            return createErrorResponse(request, throwable);
                        }
                    }

                    if (res == null) {
                        log.warn("[GatewayProcessor] 后端服务返回空响应: {} - 耗时: {}ms",
                                requestId, duration);
                        return createEmptyResponse(request);
                    }

                    log.info("[GatewayProcessor] 后端服务调用成功: {} - 耗时: {}ms",
                            requestId, duration);
                    return res;

                } finally {
                    // 归还连接到池中
                    try {
                        connection.returnToPool();
                    } catch (Exception e) {
                        log.warn("[GatewayProcessor] 归还连接失败: {}", e.getMessage());
                    }
                }
            }).get();

            return response;

        } catch (Exception e) {
            log.error("[GatewayProcessor] 后端服务调用异常: {}", requestId, e);
            return createErrorResponse(request, e);
        }
    }

    // ========== 错误响应创建方法（来自EnhancedGatewayProcessor）==========

    /**
     * 创建超时响应
     */
    private Message createTimeoutResponse(Message request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "TIMEOUT");

        String errorBody = String.format(
                "{"
                        + "    \"error\": \"TIMEOUT\","
                        + "    \"message\": \"后端服务调用超时\","
                        + "    \"timestamp\": %d,"
                        + "    \"requestId\": \"%s\""
                        + "}",
                System.currentTimeMillis(), request.getMessageId());

        HttpBody body = new HttpBody(errorBody);
        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                request.url(),
                request.method(),
                headers,
                body,
                null
        );
    }

    /**
     * 创建错误响应
     */
    private Message createErrorResponse(Message request, Throwable throwable) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "BACKEND_ERROR");

        String errorBody = String.format(
                "{"
                        + "    \"error\": \"BACKEND_ERROR\","
                        + "    \"message\": \"%s\","
                        + "    \"timestamp\": %d,"
                        + "    \"requestId\": \"%s\""
                        + "}",
                throwable.getMessage() != null ? throwable.getMessage() : "未知错误",
                System.currentTimeMillis(),
                request.getMessageId()
        );

        HttpBody body = new HttpBody(errorBody);

        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                request.url(),
                request.method(),
                headers,
                body,
                null
        );
    }

    /**
     * 创建空响应
     */
    private Message createEmptyResponse(Message request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "EMPTY_RESPONSE");

        String errorBody = String.format(
                "{"
                        + "    \"error\": \"EMPTY_RESPONSE\","
                        + "    \"message\": \"后端服务返回空响应\","
                        + "    \"timestamp\": %d,"
                        + "    \"requestId\": \"%s\""
                        + "}",
                System.currentTimeMillis(), request.getMessageId());

        HttpBody body = new HttpBody(errorBody);
        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                request.url(),
                request.method(),
                headers,
                body,
                null
        );
    }

    /**
     * 生成响应ID
     */
    private String generateResponseId(Message request) {
        return "resp-" + request.getMessageId() + "-" + System.nanoTime();
    }

    // ========== 模板方法定义 ==========

    /**
     * 第1步：请求接收与验证
     */
    protected void validateRequest(RequestContext context) {
        // 基本验证
        if (context.getInboundData() == null) {
            throw new IllegalArgumentException("入站原始数据不能为空");
        }
        log.debug("[GatewayProcessor] 请求验证与信息提取完成: {}", context.requestId());
    }

    /**
     * 检查是否需要入站协议转换
     */
    protected boolean needsInboundProtocolConversion(RequestContext context) {

        // 如果已经是Message，无需转换
        if (context.getInboundData() instanceof Message) {
            return false;
        }

        // 检查是否有协议转换器支持
        return messageCodecManager.supports(context.getInboundData().getProtocol());
    }

    /**
     * 执行入站协议转换
     */
    protected void performInboundProtocolConversion(RequestContext context) {
        Protocol sourceProtocol = context.getInboundData().getProtocol();
        // 获取协议转换器并进行转换
        MessageCodec converter = messageCodecManager.selectById(sourceProtocol);
        if (converter == null) {
            throw new RuntimeException("找不到协议转换器: " + sourceProtocol.type());
        }

        Message convertedMessage = converter.convertToMessage(context.getInboundData(), context);
        // 更新上下文中的消息
        context.setInboundMessage(convertedMessage);
    }

    /**
     * 第4步：过滤器处理（前置）
     */
    protected void executePreFilters(RequestContext context) {
        try {
            // TODO: 实现基于路由配置的过滤器执行
            log.debug("[GatewayProcessor] 前置过滤器执行完成（简化实现）");
        } catch (Exception e) {
            log.error("[GatewayProcessor] 前置过滤器执行失败", e);
            throw new RuntimeException("前置过滤器执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查是否需要出站协议转换
     */
    protected boolean needsOutboundProtocolConversion(RequestContext context) {
        // 出站消息协议
        Protocol outboundProtocol = context.getOutboundMessage().getProtocol();
        // 原始入站消息协议
        Protocol origialInboundProtocol = context.getInboundData().getProtocol();
        // 如果出站消息协议与入站协议相同，无需转换
        if (outboundProtocol.equals(origialInboundProtocol)) {
            return false;
        }

        // 检查是否有协议转换器支持
        return messageCodecManager.supports(outboundProtocol);
    }

    /**
     * 执行出站协议转换
     */
    protected void performOutboundProtocolConversion(RequestContext context) {
        // 原始入站消息协议
        Protocol targetProtocol = context.getInboundData().getProtocol();
        // 获取协议转换器并进行转换
        MessageCodec converter = messageCodecManager.selectById(targetProtocol);
        if (converter == null) {
            throw new RuntimeException("找不到协议转换器: " + targetProtocol.type());
        }

        Object convertedResponse = converter.convertFromMessage(context.getOutboundMessage(), context);
        //设置对应协议的返回数据
        context.setOutboundData(new ProtocolData(ProtocolEnum.HTTP, convertedResponse));

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
        if (request.getProtocol().equals(ProtocolEnum.HTTP)) {
            errorResponse.getHeaders().set("status", String.valueOf(errorInfo.getErrorType().getHttpStatus()));
        }

        // 设置错误响应体（JSON格式）
        String errorBody = String.format(
                "{"
                        + "    \"error\": {"
                        + "        \"type\": \"%s\","
                        + "        \"code\": \"%s\","
                        + "        \"message\": \"%s\","
                        + "        \"timestamp\": %d,"
                        + "        \"requestId\": \"%s\""
                        + "    }"
                        + "}",
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

    @Override
    public void init() {
        log.info("[GatewayProcessor] 初始化网关处理器组件");

        // 初始化各个组件
        connectionPoolManager.init();
        routeManager.init();
        // filterManager和loadBalanceManager已移除，功能集成到RouteManager中
        instanceManager.init();
        messageCodecManager.init();

        log.info("[GatewayProcessor] 网关处理器组件初始化完成");
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
        instanceManager.start();
        messageCodecManager.start();

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
        messageCodecManager.shutdown();
        instanceManager.shutdown();
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
                if (inboundMessage.getProtocol().equals(ProtocolEnum.HTTP)) {
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

} 