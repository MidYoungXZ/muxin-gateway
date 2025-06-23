package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.connect.HttpConnection;
import com.muxin.gateway.refactory.filter.*;
import com.muxin.gateway.refactory.loadbalance.DefaultLoadBalanceManager;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageType;
import com.muxin.gateway.refactory.message.NodeManager;
import com.muxin.gateway.refactory.message.http.*;
import com.muxin.gateway.refactory.node.DefaultNodeManager;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.health.HealthCheckConfig;
import com.muxin.gateway.refactory.predicate.UniversalPredicate;
import com.muxin.gateway.refactory.route.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 网关集成演示
 * 展示EnhancedGatewayProcessor如何串联所有网关组件形成完整的请求处理流程
 * 
 * @author muxin
 */
public class GatewayIntegrationDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("========== 网关集成演示开始 ==========");
        
        // 演示完整的网关处理流程
        demonstrateGatewayIntegration();
        
        System.out.println("========== 网关集成演示结束 ==========");
    }
    
    /**
     * 演示网关组件集成
     */
    public static void demonstrateGatewayIntegration() throws Exception {
        
        // ===== 第1步：创建网关处理器 =====
        System.out.println("\n=== 第1步：创建网关处理器 ===");
        EnhancedGatewayProcessor processor = createEnhancedGatewayProcessor();
        
        // ===== 第2步：配置路由和过滤器 =====
        System.out.println("\n=== 第2步：配置路由和过滤器 ===");
        configureGatewayComponents(processor);
        
        // ===== 第3步：执行请求处理流程 =====
        System.out.println("\n=== 第3步：执行请求处理流程 ===");
        executeRequestProcessingFlow(processor);
        
        // ===== 第4步：显示统计信息 =====
        System.out.println("\n=== 第4步：显示统计信息 ===");
        displayStatistics(processor);
    }
    
    /**
     * 创建增强版网关处理器
     */
    private static EnhancedGatewayProcessor createEnhancedGatewayProcessor() {
        System.out.println("创建网关核心组件...");
        
        // 创建各个管理器
        RouteManager routeManager = new SimpleRouteManager();
        FilterManager filterManager = new UniversalFilterManager();
        LoadBalanceManager loadBalanceManager = new DefaultLoadBalanceManager();
        NodeManager nodeManager = new DefaultNodeManager();
        
        // 创建增强版处理器
        EnhancedGatewayProcessor processor = new EnhancedGatewayProcessor(
            routeManager, filterManager, loadBalanceManager, nodeManager);
        
        System.out.println("✅ 网关处理器创建完成");
        return processor;
    }
    
    /**
     * 配置网关组件
     */
    private static void configureGatewayComponents(EnhancedGatewayProcessor processor) {
        // 配置路由
        configureRoutes(processor.getRouteManager());
        
        // 配置过滤器
        configureFilters(processor.getFilterManager());
        
        // 配置负载均衡
        configureLoadBalancer(processor.getLoadBalanceManager());
    }
    
    /**
     * 配置路由
     */
    private static void configureRoutes(RouteManager routeManager) {
        System.out.println("配置路由规则...");
        
        // 用户服务路由
        UniversalRoute userRoute = createTestRoute("user-service", "/api/users/**", 1);
        routeManager.addRoute(userRoute);
        
        // 订单服务路由
        UniversalRoute orderRoute = createTestRoute("order-service", "/api/orders/**", 2);
        routeManager.addRoute(orderRoute);
        
        System.out.println("✅ 路由配置完成");
    }
    
    /**
     * 配置过滤器
     */
    private static void configureFilters(FilterManager filterManager) {
        System.out.println("配置过滤器...");
        
        // 注册演示过滤器
        filterManager.registerFilter(new DemoAuthFilter());
        filterManager.registerFilter(new DemoLoggingFilter());
        
        System.out.println("✅ 过滤器配置完成");
    }
    
    /**
     * 配置负载均衡器
     */
    private static void configureLoadBalancer(LoadBalanceManager loadBalanceManager) {
        System.out.println("配置负载均衡器...");
        // 负载均衡器已经有默认配置
        System.out.println("✅ 负载均衡器配置完成");
    }
    
    /**
     * 执行请求处理流程
     */
    private static void executeRequestProcessingFlow(EnhancedGatewayProcessor processor) throws Exception {
        // 测试正常请求
        testNormalRequest(processor);
        
        // 测试失败请求
        testFailedRequest(processor);
    }
    
    /**
     * 测试正常请求
     */
    private static void testNormalRequest(EnhancedGatewayProcessor processor) throws Exception {
        System.out.println("\n--- 测试正常请求处理流程 ---");
        
        // 创建HTTP请求
        Message httpMessage = createTestHttpMessage("/api/users/123", "GET", true);
        Connection httpConnection = createTestConnection();
        UniversalRequestContext context = new DefaultRequestContext(httpMessage, httpConnection);
        
        try {
            // 执行请求处理
            CompletableFuture<Void> requestFuture = processor.processRequest(context);
            requestFuture.get();
            
            // 执行响应处理
            CompletableFuture<Void> responseFuture = processor.processResponse(context);
            responseFuture.get();
            
            System.out.println("✅ 正常请求处理成功");
            displayRequestResult(context);
            
        } catch (Exception e) {
            System.out.println("❌ 正常请求处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试失败请求
     */
    private static void testFailedRequest(EnhancedGatewayProcessor processor) throws Exception {
        System.out.println("\n--- 测试失败请求处理流程 ---");
        
        // 创建无认证的HTTP请求
        Message httpMessage = createTestHttpMessage("/api/users/456", "GET", false);
        Connection httpConnection = createTestConnection();
        UniversalRequestContext context = new DefaultRequestContext(httpMessage, httpConnection);
        
        try {
            // 执行请求处理
            CompletableFuture<Void> requestFuture = processor.processRequest(context);
            requestFuture.get();
            
            System.out.println("❌ 预期失败的请求却成功了");
            
        } catch (Exception e) {
            System.out.println("✅ 预期失败的请求正确失败: " + e.getMessage());
            
            // 处理错误
            processor.processError(context, e);
        }
    }
    
    /**
     * 显示请求处理结果
     */
    private static void displayRequestResult(UniversalRequestContext context) {
        String traceId = context.getAttribute("traceId", String.class);
        String routeId = context.getAttribute("routeId", String.class);
        String targetAddress = context.getAttribute("targetAddress", String.class);
        Long startTime = context.getAttribute("startTime", Long.class);
        
        System.out.println("请求处理结果:");
        System.out.printf("  TraceId: %s\n", traceId);
        System.out.printf("  RouteId: %s\n", routeId);
        System.out.printf("  Target: %s\n", targetAddress);
        if (startTime != null) {
            System.out.printf("  Duration: %dms\n", System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 显示统计信息
     */
    private static void displayStatistics(EnhancedGatewayProcessor processor) {
        Map<String, Object> stats = processor.getStatistics();
        
        System.out.println("网关统计信息:");
        stats.forEach((key, value) -> {
            System.out.printf("  %s: %s\n", key, value);
        });
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建测试路由
     */
    private static UniversalRoute createTestRoute(String id, String pathPattern, int order) {
        return new DefaultRoute.Builder()
            .id(id)
            .name(id + " 路由")
            .order(order)
            .predicates(Arrays.asList(new TestPathPredicate(pathPattern)))
            .filters(Arrays.asList())
            .target(new TestRouteTarget(id))
            .build();
    }
    
    /**
     * 创建测试HTTP消息
     */
    private static Message createTestHttpMessage(String path, String method, boolean withAuth) {
        String messageId = "msg-" + System.nanoTime();
        
        // 创建消息头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Host", "api.example.com");
        headers.set("method", method);
        headers.set("uri", path);
        
        if (withAuth) {
            headers.set("Authorization", "Bearer test-token");
        }
        
        // 创建消息体
        HttpBody body = new HttpBody("{}".getBytes());
        
        // 创建元数据
        HttpMetadata metadata = new HttpMetadata();
        metadata.setAttribute("method", method);
        metadata.setAttribute("path", path);
        
        return new HttpMessage(messageId, MessageType.REQUEST,
            new HttpProtocol(), headers, body, metadata);
    }
    
    /**
     * 创建测试连接
     */
    private static Connection createTestConnection() {
        HttpEndpointAddress localAddr = new HttpEndpointAddress("http://localhost:8080");
        HttpEndpointAddress remoteAddr = new HttpEndpointAddress("http://client:12345");
        return new HttpConnection(new HttpProtocol(), localAddr, remoteAddr);
    }
    
    // ========== 测试组件实现 ==========
    
    /**
     * 演示认证过滤器
     */
    private static class DemoAuthFilter implements UniversalFilter {
        
        @Override
        public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
            System.out.println("🔐 执行认证过滤器");
            
            String authHeader = context.getInboundMessage().getHeaders().get("Authorization", String.class);
            if (authHeader == null) {
                throw new RuntimeException("认证失败: 缺少Authorization头");
            }
            
            context.setAttribute("authenticated", true);
            System.out.println("✅ 认证通过");
            
            if (chain != null) {
                chain.filter(context);
            }
        }
        
        @Override
        public String getName() { return "demo-auth"; }
        
        @Override
        public FilterType getType() { return FilterType.PRE; }
        
        @Override
        public Set<Protocol> getSupportedProtocols() { 
            return Set.of(new HttpProtocol()); 
        }
        
        @Override
        public boolean isEnabled() { return true; }
        
        @Override
        public int getOrder() { return 1; }
    }
    
    /**
     * 演示日志过滤器
     */
    private static class DemoLoggingFilter implements UniversalFilter {
        
        @Override
        public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
            String method = context.getAttribute("requestMethod", String.class);
            String path = context.getAttribute("requestPath", String.class);
            
            System.out.printf("📝 请求日志: %s %s\n", method, path);
            
            if (chain != null) {
                chain.filter(context);
            }
        }
        
        @Override
        public String getName() { return "demo-logging"; }
        
        @Override
        public FilterType getType() { return FilterType.PRE; }
        
        @Override
        public Set<Protocol> getSupportedProtocols() { 
            return Set.of(new HttpProtocol()); 
        }
        
        @Override
        public boolean isEnabled() { return true; }
        
        @Override
        public int getOrder() { return 2; }
    }
    
    /**
     * 测试路径断言
     */
    private static class TestPathPredicate implements UniversalPredicate {
        private final String pathPattern;
        
        public TestPathPredicate(String pathPattern) {
            this.pathPattern = pathPattern;
        }
        
        @Override
        public boolean test(UniversalRequestContext context) {
            String requestPath = context.getAttribute("requestPath", String.class);
            if (requestPath == null) {
                return false;
            }
            
            // 简单的模式匹配
            String pattern = pathPattern.replace("**", ".*");
            boolean matches = requestPath.matches(pattern);
            
            System.out.printf("🔍 路径断言: %s 匹配 %s = %s\n", requestPath, pathPattern, matches);
            return matches;
        }
        
        @Override
        public String getType() { return "PATH"; }
        
        @Override
        public String getName() { return "test-path"; }
        
        @Override
        public Set<Protocol> getSupportedProtocols() { 
            return Set.of(new HttpProtocol()); 
        }
        
        @Override
        public Map<String, Object> getConfig() {
            return Map.of("pathPattern", pathPattern);
        }
    }
    
    /**
     * 测试路由目标
     */
    private static class TestRouteTarget implements RouteTarget {
        private final String serviceName;
        
        public TestRouteTarget(String serviceName) {
            this.serviceName = serviceName;
        }
        
        @Override
        public Protocol getTargetProtocol() {
            return new HttpProtocol();
        }
        
        @Override
        public List<EndpointAddress> getTargetAddresses() {
            return Arrays.asList(
                new HttpEndpointAddress("http://" + serviceName + "-1:8080"),
                new HttpEndpointAddress("http://" + serviceName + "-2:8080")
            );
        }
        
        @Override
        public String getLoadBalanceStrategy() {
            return "ROUND_ROBIN";
        }
        
        @Override
        public Map<String, Object> getTargetConfig() {
            return Map.of("timeout", 30000);
        }
        
        @Override
        public HealthCheckConfig getHealthCheckConfig() {
            return null;
        }
        
        @Override
        public EndpointAddress selectTarget(UniversalRequestContext context) {
            return getTargetAddresses().get(0);
        }
    }
} 