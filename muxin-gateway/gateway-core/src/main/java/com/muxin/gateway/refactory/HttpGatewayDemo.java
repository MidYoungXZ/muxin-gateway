package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.http.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP网关演示类
 * 展示完整的HTTP协议转发功能
 * 
 * @author muxin
 */
public class HttpGatewayDemo {
    
    public static void main(String[] args) {
        System.out.println("🚀 启动 HTTP 协议网关演示...\n");
        
        try {
            // 创建并配置网关
            SimpleMultiProtocolGateway gateway = createAndConfigureGateway();
            
            // 启动网关
            gateway.start();
            
            // 模拟HTTP请求处理
            simulateHttpRequests(gateway);
            
            // 停止网关
            gateway.stop();
            
            System.out.println("\n✅ HTTP 协议网关演示完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建并配置网关
     */
    private static SimpleMultiProtocolGateway createAndConfigureGateway() {
        System.out.println("📝 配置网关组件...");
        
        // 创建网关
        SimpleMultiProtocolGateway gateway = new SimpleMultiProtocolGateway();
        
        // 获取管理器组件
        GatewayProcessor processor = gateway.getGatewayProcessor();
        RouteManager routeManager = processor.getRouteManager();
        FilterManager filterManager = processor.getFilterManager();
        NodeManager nodeManager = processor.getNodeManager();
        
        // 配置路由
        configureRoutes(routeManager);
        
        // 配置过滤器
        configureFilters(filterManager);
        
        // 配置服务节点
        configureServiceNodes(nodeManager);
        
        System.out.println("✅ 网关配置完成\n");
        return gateway;
    }
    
    /**
     * 配置路由
     */
    private static void configureRoutes(RouteManager routeManager) {
        System.out.println("🛣️  配置路由规则...");
        
        // HTTP协议
        Protocol httpProtocol = new HttpProtocol();
        
        // 创建路由目标
        RouteTarget userServiceTarget = createRouteTarget("user-service", 
            "http://localhost:8081", "http://localhost:8082");
        
        RouteTarget orderServiceTarget = createRouteTarget("order-service",
            "http://localhost:8083", "http://localhost:8084");
        
        // 用户服务路由
        UniversalRoute userRoute = new DefaultRoute.Builder("user-route", "用户服务路由")
            .description("处理用户相关请求")
            .order(10)
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/users/**"))
            .predicate(new HttpMethodPredicate("GET", "POST", "PUT", "DELETE"))
            .target(userServiceTarget)
            .metadata("service", "user-service")
            .build();
        
        // 订单服务路由  
        UniversalRoute orderRoute = new DefaultRoute.Builder("order-route", "订单服务路由")
            .description("处理订单相关请求")
            .order(20)
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/orders/**"))
            .predicate(new HttpMethodPredicate("GET", "POST"))
            .target(orderServiceTarget)
            .metadata("service", "order-service")
            .build();
        
        // 默认路由
        RouteTarget defaultTarget = createRouteTarget("default-service", "http://localhost:8080");
        UniversalRoute defaultRoute = new DefaultRoute.Builder("default-route", "默认路由")
            .description("默认路由处理")
            .order(999)
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/**"))
            .target(defaultTarget)
            .build();
        
        // 添加路由
        routeManager.addRoute(userRoute);
        routeManager.addRoute(orderRoute);
        routeManager.addRoute(defaultRoute);
        
        System.out.println("  ✓ 添加用户服务路由: /api/users/**");
        System.out.println("  ✓ 添加订单服务路由: /api/orders/**");
        System.out.println("  ✓ 添加默认路由: /**");
    }
    
    /**
     * 配置过滤器
     */
    private static void configureFilters(FilterManager filterManager) {
        System.out.println("🔧 配置过滤器...");
        
        // 注册过滤器
        filterManager.registerFilter(new HttpAuthFilter());
        filterManager.registerFilter(new HttpLoggingFilter());
        
        System.out.println("  ✓ 注册认证过滤器");
        System.out.println("  ✓ 注册日志过滤器");
    }
    
    /**
     * 配置服务节点
     */
    private static void configureServiceNodes(NodeManager nodeManager) {
        System.out.println("🖥️  配置服务节点...");
        
        Protocol httpProtocol = new HttpProtocol();
        
        // 用户服务节点
        EndpointAddress userAddr1 = new HttpEndpointAddress("http://localhost:8081");
        EndpointAddress userAddr2 = new HttpEndpointAddress("http://localhost:8082");
        
        UniversalServiceNode userNode1 = new DefaultServiceNode("user-node-1", "user-service", userAddr1, httpProtocol);
        UniversalServiceNode userNode2 = new DefaultServiceNode("user-node-2", "user-service", userAddr2, httpProtocol);
        
        // 订单服务节点
        EndpointAddress orderAddr1 = new HttpEndpointAddress("http://localhost:8083");
        EndpointAddress orderAddr2 = new HttpEndpointAddress("http://localhost:8084");
        
        UniversalServiceNode orderNode1 = new DefaultServiceNode("order-node-1", "order-service", orderAddr1, httpProtocol);
        UniversalServiceNode orderNode2 = new DefaultServiceNode("order-node-2", "order-service", orderAddr2, httpProtocol);
        
        // 添加节点
        nodeManager.addNode(userNode1);
        nodeManager.addNode(userNode2);
        nodeManager.addNode(orderNode1);
        nodeManager.addNode(orderNode2);
        
        System.out.println("  ✓ 添加用户服务节点: localhost:8081, localhost:8082");
        System.out.println("  ✓ 添加订单服务节点: localhost:8083, localhost:8084");
    }
    
    /**
     * 模拟HTTP请求处理
     */
    private static void simulateHttpRequests(SimpleMultiProtocolGateway gateway) throws Exception {
        System.out.println("📨 模拟HTTP请求处理...\n");
        
        // 模拟不同的HTTP请求
        simulateRequest(gateway, "GET", "/api/users/123", "获取用户信息");
        Thread.sleep(100);
        
        simulateRequest(gateway, "POST", "/api/users", "创建用户");
        Thread.sleep(100);
        
        simulateRequest(gateway, "GET", "/api/orders/456", "获取订单信息");
        Thread.sleep(100);
        
        simulateRequest(gateway, "POST", "/api/orders", "创建订单");
        Thread.sleep(100);
        
        simulateRequest(gateway, "GET", "/api/health", "健康检查");
        Thread.sleep(100);
    }
    
    /**
     * 模拟单个请求
     */
    private static void simulateRequest(SimpleMultiProtocolGateway gateway, 
                                      String method, String path, String description) {
        try {
            System.out.println(String.format("🔄 处理请求: %s %s (%s)", method, path, description));
            
            // 创建HTTP消息
            Message httpMessage = createHttpMessage(method, path);
            
            // 创建HTTP连接
            Protocol httpProtocol = new HttpProtocol();
            EndpointAddress localAddr = new HttpEndpointAddress("http://localhost:8080");
            EndpointAddress remoteAddr = new HttpEndpointAddress("http://client:0");
            Connection httpConnection = new HttpConnection(httpProtocol, localAddr, remoteAddr);
            
            // 处理请求
            CompletableFuture<Message> future = gateway.handleInbound(httpMessage, httpConnection);
            Message response = future.get();
            
            System.out.println(String.format("  ✅ 响应: %s (耗时: %dms)\n", 
                response.getMessageId(), 50)); // 模拟耗时
                
        } catch (Exception e) {
            System.err.println(String.format("  ❌ 请求失败: %s\n", e.getMessage()));
        }
    }
    
    /**
     * 创建HTTP消息
     */
    private static Message createHttpMessage(String method, String path) {
        String messageId = "msg-" + System.nanoTime();
        Protocol httpProtocol = new HttpProtocol();
        
        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestLine", method + " " + path + " HTTP/1.1");
        headers.set("Host", "localhost:8080");
        headers.set("User-Agent", "MuxinGateway/1.0");
        headers.set("Accept", "application/json");
        
        // 创建请求体
        String bodyContent = method.equals("POST") ? "{\"data\": \"test\"}" : "";
        HttpBody body = new HttpBody(bodyContent.getBytes());
        
        // 创建元数据
        HttpMetadata metadata = new HttpMetadata();
        metadata.setMethod(method);
        metadata.setPath(path);
        metadata.setTraceId("trace-" + System.nanoTime());
        
        return new HttpMessage(messageId, MessageType.REQUEST, httpProtocol, headers, body, metadata);
    }
    
    /**
     * 创建路由目标
     */
    private static RouteTarget createRouteTarget(String serviceName, String... urls) {
        List<EndpointAddress> endpoints = new ArrayList<>();
        for (String url : urls) {
            endpoints.add(new HttpEndpointAddress(url));
        }
        return new DefaultRouteTarget(serviceName, endpoints);
    }
    
    /**
     * 默认路由目标实现
     */
    private static class DefaultRouteTarget implements RouteTarget {
        private final String serviceName;
        private final List<EndpointAddress> endpoints;
        
        public DefaultRouteTarget(String serviceName, List<EndpointAddress> endpoints) {
            this.serviceName = serviceName;
            this.endpoints = new ArrayList<>(endpoints);
        }
        
        @Override
        public String getServiceName() {
            return serviceName;
        }
        
        @Override
        public List<EndpointAddress> getAvailableEndpoints() {
            return new ArrayList<>(endpoints);
        }
        
        @Override
        public EndpointAddress getPrimaryEndpoint() {
            return endpoints.isEmpty() ? null : endpoints.get(0);
        }
        
        @Override
        public Map<String, Object> getMetadata() {
            return Map.of("service", serviceName, "endpointCount", endpoints.size());
        }
    }
} 