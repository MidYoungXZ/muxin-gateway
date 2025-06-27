package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.connect.ConnectionFactoryManager;
import com.muxin.gateway.refactory.filter.FilterManager;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.ProtocolConverter;
import com.muxin.gateway.refactory.message.ProtocolConverterManager;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.NodeManager;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 重新设计的网关核心处理器抽象类
 * 使用模板方法模式定义完整的处理流程
 * 集成所有网关逻辑：协议转换、路由、过滤器、负载均衡等
 *
 * @author muxin
 */
@Slf4j
public abstract class GatewayProcessor {

    // ========== 核心组件 ==========
    protected final RouteManager routeManager;
    protected final FilterManager filterManager;
    protected final LoadBalanceManager loadBalanceManager;
    protected final NodeManager nodeManager;
    
    // ========== 新增组件（替代ProtocolAdapter） ==========
    protected final ProtocolConverterManager converterManager;
    protected final ConnectionFactoryManager connectionFactoryManager;
    
    // ========== 线程池管理 ==========
    protected final Executor gatewayExecutor;

    // ========== 构造方法 ==========

    /**
     * 构造网关处理器
     *
     * @param routeManager             路由管理器
     * @param filterManager            过滤器管理器
     * @param loadBalanceManager       负载均衡管理器
     * @param nodeManager              节点管理器
     * @param converterManager         协议转换管理器
     * @param connectionFactoryManager 连接工厂管理器
     */
    protected GatewayProcessor(RouteManager routeManager,
                             FilterManager filterManager,
                             LoadBalanceManager loadBalanceManager,
                             NodeManager nodeManager,
                             ProtocolConverterManager converterManager,
                             ConnectionFactoryManager connectionFactoryManager) {
        this(routeManager, filterManager, loadBalanceManager, nodeManager, 
             converterManager, connectionFactoryManager, ForkJoinPool.commonPool());
    }
    
    protected GatewayProcessor(RouteManager routeManager,
                             FilterManager filterManager,
                             LoadBalanceManager loadBalanceManager,
                             NodeManager nodeManager,
                             ProtocolConverterManager converterManager,
                             ConnectionFactoryManager connectionFactoryManager,
                             Executor executor) {
        this.routeManager = Objects.requireNonNull(routeManager, "RouteManager不能为空");
        this.filterManager = Objects.requireNonNull(filterManager, "FilterManager不能为空");
        this.loadBalanceManager = Objects.requireNonNull(loadBalanceManager, "LoadBalanceManager不能为空");
        this.nodeManager = Objects.requireNonNull(nodeManager, "NodeManager不能为空");
        this.converterManager = Objects.requireNonNull(converterManager, "ProtocolConverterManager不能为空");
        this.connectionFactoryManager = Objects.requireNonNull(connectionFactoryManager, "ConnectionFactoryManager不能为空");
        this.gatewayExecutor = Objects.requireNonNull(executor, "Executor不能为空");
        
        log.info("[GatewayProcessor] 网关处理器初始化完成 - Executor: {}, 支持协议: {}", 
            executor.getClass().getSimpleName(), 
            converterManager.getSupportedSourceProtocols());
    }


    // ========== 主要处理入口（模板方法） ==========

    /**
     * 完整的网关处理流程
     * 使用模板方法模式，定义标准处理流程
     * final 修饰，防止子类重写核心流程
     */
    public final CompletableFuture<Message> process(UniversalRequestContext context) {
        String traceId = generateTraceId();
        context.setAttribute("traceId", traceId);
        context.setAttribute("startTime", System.currentTimeMillis());

        log.debug("[GATEWAY_PROCESSOR] 开始处理请求 - TraceId: {}", traceId);

        return executeProcessingPipeline(context)
                .exceptionally(ex ->
                        handleProcessingError(context, ex)
                );
    }

        /**
     * 处理流水线（可以被子类重写以自定义流程）
     * 一次切换优化版本：前6步同步执行，步骤7开始异步执行
     */
    protected CompletableFuture<Message> executeProcessingPipeline(UniversalRequestContext context) {
        try {
            // ============ Phase 1: 同步快速处理（当前线程：通常是Netty I/O Thread）============
            // 第1步：请求验证和预处理
            validateAndPreprocess(context);
            
            // 第2步：入站协议适配
            executeInboundProtocolAdaptation(context);
            
            // 第3步：路由匹配
            executeRouteMatching(context);
            
            // 第4步：前置过滤器链
            executePreFilters(context);
            
            // 第5步：负载均衡和节点选择
            executeLoadBalancing(context);
            
            // 第6步：路由级过滤器链
            executeRouteFilters(context);
            
            // ============ Phase 2: 一次性切换到异步线程执行剩余步骤 ============
            return CompletableFuture.supplyAsync(() -> {
                // 在同一个Gateway Worker线程中执行步骤7-10，无进一步切换
                
                // 第7步：后端服务调用
                EndpointAddress target = (EndpointAddress) context.getSelectedNode();
                Message request = context.getInboundMessage();
                Message response = invokeBackendService(context, target, request);
                context.setOutboundMessage(response);
                log.debug("[GATEWAY_PROCESSOR] 后端服务调用完成 - Target: {}", target.toUri());
                
                // 第8步：后置过滤器链
                executePostFiltersSync(context);
                
                // 第9步：出站协议适配
                executeOutboundProtocolAdaptationSync(context);
                
                // 第10步：最终处理
                return finalizeResponse(context);
                
            }, gatewayExecutor);
            
        } catch (Exception e) {
            log.error("[GATEWAY_PROCESSOR] 同步处理阶段异常", e);
            return CompletableFuture.completedFuture(handleProcessingError(context, e));
        }
    }

    // ========== 各步骤的默认实现（可重写） ==========

    /**
     * 第1步：请求验证和预处理
     */
    protected void validateAndPreprocess(UniversalRequestContext context) {
        // 基础验证
        if (context.getInboundConnection() == null) {
            throw new GatewayException("入站连接不能为空");
        }

        // 提取基础信息
        extractBasicRequestInfo(context);

        // 设置默认属性
        setDefaultContextAttributes(context);

        log.debug("[GATEWAY_PROCESSOR] 请求验证和预处理完成");
    }

        /**
     * 第2步：入站协议适配（同步版本）
     */
    protected void executeInboundProtocolAdaptation(UniversalRequestContext context) {
        // 获取协议特定的请求对象
        Object protocolSpecific = context.getAttribute("protocolRequest", Object.class);
        if (protocolSpecific == null) {
            throw new GatewayException("协议特定请求对象不能为空");
        }
        
        // 检测协议类型
        Protocol sourceProtocol = detectProtocol(protocolSpecific, context);
        
        // 获取协议转换器
        ProtocolConverter converter = converterManager.getConverter(sourceProtocol, Protocol.UNIVERSAL);
        if (converter == null) {
            throw new GatewayException("未找到协议转换器: " + sourceProtocol.getName() + " -> Universal");
        }
        
        // 执行协议转换
        Message universalMessage = converter.convertToUniversal(protocolSpecific, context);
        context.setInboundMessage(universalMessage);

        log.debug("[GATEWAY_PROCESSOR] 入站协议适配完成 - 协议: {} -> Universal",
            sourceProtocol.getName());
    }

    /**
     * 第3步：路由匹配（同步版本）
     */
    protected void executeRouteMatching(UniversalRequestContext context) {
        UniversalRoute matchedRoute = matchRoute(context);
        if (matchedRoute == null) {
            throw new GatewayException("未找到匹配的路由");
        }

        context.setMatchedRoute(matchedRoute);
        log.debug("[GATEWAY_PROCESSOR] 路由匹配成功 - RouteId: {}", matchedRoute.getId());
    }

    /**
     * 第4步：前置过滤器链（同步版本）
     */
    protected void executePreFilters(UniversalRequestContext context) {
        Protocol protocol = context.getInboundProtocol();
        if (protocol != null && filterManager != null) {
            // TODO: 需要实现 FilterManager.getFilters(FilterType) 方法
            // 临时跳过过滤器执行
            log.debug("[GATEWAY_PROCESSOR] 前置过滤器链执行完成（暂时跳过）");
        }
        log.debug("[GATEWAY_PROCESSOR] 前置过滤器链执行完成");
    }

    /**
     * 第5步：负载均衡和节点选择（同步版本）
     */
    protected void executeLoadBalancing(UniversalRequestContext context) {
        UniversalRoute route = (UniversalRoute) context.getMatchedRoute();
        EndpointAddress targetNode = selectTargetNode(context, route);

        if (targetNode == null) {
            throw new GatewayException("负载均衡未能选择目标节点");
        }

        context.setSelectedNode(targetNode);
        log.debug("[GATEWAY_PROCESSOR] 负载均衡选择完成 - Target: {}", targetNode.toUri());
    }

    /**
     * 第6步：路由级过滤器链（同步版本）
     */
    protected void executeRouteFilters(UniversalRequestContext context) {
        UniversalRoute route = (UniversalRoute) context.getMatchedRoute();
        if (route != null && route.getFilters() != null) {
            for (UniversalFilter filter : route.getFilters()) {
                if (filter.isEnabled()) {
                    filter.filter(context, null);
                }
            }
        }
        log.debug("[GATEWAY_PROCESSOR] 路由过滤器链执行完成");
    }



    /**
     * 第8步：后置过滤器链（同步版本，用于异步线程内部）
     */
    protected void executePostFiltersSync(UniversalRequestContext context) {
        Protocol protocol = context.getInboundProtocol();
        if (protocol != null && filterManager != null) {
            // TODO: 需要实现 FilterManager.getFilters(FilterType) 方法
            // 临时跳过过滤器执行
            log.debug("[GATEWAY_PROCESSOR] 后置过滤器链执行完成（暂时跳过）");
        }
        log.debug("[GATEWAY_PROCESSOR] 后置过滤器链执行完成");
    }



    /**
     * 第9步：出站协议适配（同步版本，用于异步线程内部）
     */
    protected void executeOutboundProtocolAdaptationSync(UniversalRequestContext context) {
        Message universalResponse = context.getOutboundMessage();
        if (universalResponse == null) {
            log.debug("[GATEWAY_PROCESSOR] 出站消息为空，跳过协议适配");
            return;
        }
        
        // 确定目标协议
        Protocol targetProtocol = determineTargetProtocol(context);
        
        // 如果目标协议是通用协议，则不需要转换
        if (Protocol.UNIVERSAL.equals(targetProtocol)) {
            log.debug("[GATEWAY_PROCESSOR] 目标协议为Universal，跳过协议适配");
            return;
        }
        
        // 获取协议转换器
        ProtocolConverter converter = converterManager.getConverter(Protocol.UNIVERSAL, targetProtocol);
        if (converter == null) {
            log.warn("[GATEWAY_PROCESSOR] 未找到协议转换器: Universal -> {}, 使用原始响应", 
                targetProtocol.getName());
            return;
        }
        
        // 执行协议转换
        Object protocolResponse = converter.convertFromUniversal(universalResponse, context);
        context.setAttribute("protocolResponse", protocolResponse);

        log.debug("[GATEWAY_PROCESSOR] 出站协议适配完成: Universal -> {}", targetProtocol.getName());
    }



    /**
     * 第10步：最终处理
     */
    protected Message finalizeResponse(UniversalRequestContext context) {
        Message response = context.getOutboundMessage();

        // 设置响应头
        if (response != null) {
            response.getHeaders().set("X-Gateway-Version", "2.0");
            response.getHeaders().set("X-Trace-Id", context.getAttribute("traceId", String.class));
            response.getHeaders().set("X-Processing-Time",
                    String.valueOf(System.currentTimeMillis() - context.getAttribute("startTime", Long.class)));
        }

        log.debug("[GATEWAY_PROCESSOR] 响应处理完成 - TraceId: {}",
                context.getAttribute("traceId", String.class));

        return response;
    }

    // ========== 错误处理 ==========

    /**
     * 处理流程中的错误
     */
    protected Message handleProcessingError(UniversalRequestContext context, Throwable ex) {
        log.error("[GATEWAY_PROCESSOR] 处理请求异常 - TraceId: {}",
                context.getAttribute("traceId", String.class), ex);

        // 分类错误
        ErrorType errorType = classifyError(ex);

        // 创建错误响应
        Message errorResponse = createErrorResponse(context, errorType, ex);
        context.setOutboundMessage(errorResponse);

        // 记录错误指标
        recordErrorMetrics(context, ex);

        return errorResponse;
    }

    // ========== 抽象方法（子类必须实现） ==========

    /**
     * 检测协议类型
     * 
     * @param protocolSpecific 协议特定的数据对象
     * @param context 请求上下文
     * @return 检测到的协议
     */
    protected abstract Protocol detectProtocol(Object protocolSpecific, UniversalRequestContext context);

    /**
     * 确定目标协议类型
     * 
     * @param context 请求上下文
     * @return 目标协议
     */
    protected abstract Protocol determineTargetProtocol(UniversalRequestContext context);

    /**
     * 路由匹配
     */
    public abstract UniversalRoute matchRoute(UniversalRequestContext context);

    /**
     * 负载均衡节点选择
     */
    public abstract EndpointAddress selectTargetNode(UniversalRequestContext context, UniversalRoute route);

    /**
     * 后端服务调用
     */
    public abstract Message invokeBackendService(UniversalRequestContext context, EndpointAddress target, Message request);

    /**
     * 创建错误响应
     */
    public abstract Message createErrorResponse(UniversalRequestContext context, ErrorType errorType, Throwable ex);

    // ========== 组件访问器 ==========

    public final RouteManager getRouteManager() {
        return routeManager;
    }

    public final FilterManager getFilterManager() {
        return filterManager;
    }

    public final LoadBalanceManager getLoadBalanceManager() {
        return loadBalanceManager;
    }

    public final NodeManager getNodeManager() {
        return nodeManager;
    }
    
    public final ProtocolConverterManager getConverterManager() {
        return converterManager;
    }
    
    public final ConnectionFactoryManager getConnectionFactoryManager() {
        return connectionFactoryManager;
    }

    // ========== 辅助方法（可重写） ==========

    protected void extractBasicRequestInfo(UniversalRequestContext context) {
        Connection connection = context.getInboundConnection();
        if (connection != null) {
            context.setAttribute("clientAddress", connection.getRemoteAddress());
            context.setAttribute("serverAddress", connection.getLocalAddress());
            context.setAttribute("protocol", connection.getProtocol());
        }
    }

    protected void setDefaultContextAttributes(UniversalRequestContext context) {
        context.setAttribute("timeout", 30000); // 30秒默认超时
        context.setAttribute("maxRetries", 3);   // 3次重试
    }

    protected String generateTraceId() {
        return "trace-" + System.currentTimeMillis() + "-" +
                Long.toHexString(System.nanoTime()).substring(8);
    }

    protected ErrorType classifyError(Throwable ex) {
        if (ex instanceof GatewayException) {
            return ErrorType.GATEWAY_ERROR;
        } else if (ex instanceof RuntimeException) {
            return ErrorType.INTERNAL_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }

    protected void recordErrorMetrics(UniversalRequestContext context, Throwable ex) {
        // 记录错误指标的默认实现
        log.warn("[GATEWAY_PROCESSOR] 记录错误指标 - Error: {}", ex.getMessage());
    }

    // ========== 内部类定义 ==========

    /**
     * 网关异常
     */
    class GatewayException extends RuntimeException {
        public GatewayException(String message) {
            super(message);
        }

        public GatewayException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        ROUTE_NOT_FOUND,
        TARGET_NOT_FOUND,
        LOAD_BALANCE_FAILED,
        BACKEND_INVOCATION_FAILED,
        PROTOCOL_CONVERSION_FAILED,
        FILTER_EXECUTION_FAILED,
        GATEWAY_ERROR,
        INTERNAL_ERROR,
        UNKNOWN_ERROR
    }
} 