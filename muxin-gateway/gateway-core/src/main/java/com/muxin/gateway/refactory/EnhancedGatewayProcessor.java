package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.filter.FilterManager;
import com.muxin.gateway.refactory.filter.FilterType;
import com.muxin.gateway.refactory.filter.UniversalFilter;
import com.muxin.gateway.refactory.filter.UniversalFilterChain;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageMetadata;
import com.muxin.gateway.refactory.message.NodeManager;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强版网关处理器实现
 * 整合所有网关组件，提供完整的请求处理流程
 * 
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
public class EnhancedGatewayProcessor implements GatewayProcessor {
    
    // ========== 核心组件 ==========
    private final RouteManager routeManager;
    private final FilterManager filterManager;
    private final LoadBalanceManager loadBalanceManager;
    private final NodeManager nodeManager;
    
    // ========== 增强组件 ==========
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
        this.routeManager = routeManager;
        this.filterManager = filterManager;
        this.loadBalanceManager = loadBalanceManager;
        this.nodeManager = nodeManager;
        
        // 初始化增强组件
        this.gatewayMetrics = new GatewayMetrics();
        this.gatewayConfig = new GatewayConfig();
        
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 增强版网关处理器初始化完成");
    }
    
    @Override
    public CompletableFuture<Void> processRequest(UniversalRequestContext context) {
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            String traceId = generateTraceId();
            
            try {
                // 设置跟踪信息
                context.setAttribute("traceId", traceId);
                context.setAttribute("startTime", startTime);
                
                requestCounter.incrementAndGet();
                
                System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 开始处理请求 - TraceId: %s, MessageId: %s", 
                    traceId, context.getInboundMessage().getMessageId()));
                
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
                
                // ===== 第6步：后端服务调用 =====
                executeBackendInvocation(context, targetEndpoint);
                
                // ===== 第7步：记录成功指标 =====
                recordSuccessMetrics(context, startTime);
                
                successCounter.incrementAndGet();
                System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 请求处理成功 - TraceId: %s, Duration: %dms", 
                    traceId, System.currentTimeMillis() - startTime));
                
            } catch (Exception e) {
                // 处理异常
                handleRequestException(context, e, startTime, traceId);
                throw e;
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> processResponse(UniversalRequestContext context) {
        return CompletableFuture.runAsync(() -> {
            String traceId = context.getAttribute("traceId", String.class);
            
            try {
                System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 开始处理响应 - TraceId: %s", traceId));
                
                // ===== 第1步：后置过滤器链 =====
                executePostFilters(context);
                
                // ===== 第2步：响应后处理 =====
                executePostProcessing(context);
                
                // 标记处理完成
                context.markComplete();
                
                System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 响应处理完成 - TraceId: %s", traceId));
                
            } catch (Exception e) {
                System.err.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 响应处理失败 - TraceId: %s, Error: %s", 
                    traceId, e.getMessage()));
                processError(context, e);
            }
        });
    }
    
    @Override
    public void processError(UniversalRequestContext context, Exception exception) {
        String traceId = context.getAttribute("traceId", String.class);
        Long startTime = context.getAttribute("startTime", Long.class);
        
        try {
            System.err.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 处理错误 - TraceId: %s, Error: %s", 
                traceId, exception.getMessage()));
            
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
            System.err.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 错误处理失败 - TraceId: %s, Error: %s", 
                traceId, e.getMessage()));
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
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行请求前处理");
        
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
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行路由匹配");
        
        UniversalRoute matchedRoute = routeManager.matchRoute(context);
        if (matchedRoute == null) {
            throw new RuntimeException("未找到匹配的路由");
        }
        
        context.setMatchedRoute(matchedRoute);
        context.setAttribute("routeId", matchedRoute.getId());
        
        System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 路由匹配成功 - RouteId: %s", 
            matchedRoute.getId()));
        
        return matchedRoute;
    }
    
    /**
     * 第3步：前置过滤器处理
     */
    private void executePreFilters(UniversalRequestContext context, UniversalRoute route) {
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行前置过滤器链");
        
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
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行负载均衡");
        
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
        
        System.out.println(String.format("[ENHANCED_GATEWAY_PROCESSOR] 负载均衡选择成功 - Target: %s", 
            selectedTarget.toUri()));
        
        return selectedTarget;
    }
    
    /**
     * 第5步：路由过滤器处理
     */
    private void executeRouteFilters(UniversalRequestContext context, UniversalRoute route) {
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行路由过滤器链");
        
        // 执行路由特定的过滤器
        for (UniversalFilter filter : route.getFilters()) {
            try {
                if (filter.isEnabled() && 
                    filter.getSupportedProtocols().contains(context.getInboundProtocol())) {
                    filter.filter(context, null);
                }
            } catch (Exception e) {
                System.err.println(String.format("路由过滤器执行失败: %s, 错误: %s", 
                    filter.getName(), e.getMessage()));
                throw new RuntimeException("路由过滤器执行失败: " + filter.getName(), e);
            }
        }
    }
    
    /**
     * 第6步：后端服务调用
     */
    private void executeBackendInvocation(UniversalRequestContext context, EndpointAddress target) {
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行后端服务调用");
        
        try {
            // 执行实际调用
            Message response = performBackendCall(context, target);
            context.setOutboundMessage(response);
            
            System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 后端服务调用成功");
            
        } catch (Exception e) {
            throw new RuntimeException("后端服务调用失败", e);
        }
    }
    
    /**
     * 第1步（响应）：后置过滤器处理
     */
    private void executePostFilters(UniversalRequestContext context) {
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行后置过滤器链");
        
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
        System.out.println("[ENHANCED_GATEWAY_PROCESSOR] 执行响应后处理");
        
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
     * 执行实际的后端调用
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