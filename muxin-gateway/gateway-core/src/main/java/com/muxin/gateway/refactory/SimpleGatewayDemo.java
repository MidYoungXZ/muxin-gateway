package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.connect.HttpConnection;
import com.muxin.gateway.refactory.filter.FilterManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageType;
import com.muxin.gateway.refactory.message.NodeManager;
import com.muxin.gateway.refactory.message.http.*;
import com.muxin.gateway.refactory.node.DefaultServiceNode;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.UniversalServiceNode;
import com.muxin.gateway.refactory.node.health.HealthCheckConfig;
import com.muxin.gateway.refactory.route.DefaultRoute;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 简单网关演示类
 * 展示HTTP协议转发的基本功能
 * 
 * @author muxin
 */
public class SimpleGatewayDemo {
    
    public static void main(String[] args) {
        System.out.println("🚀 启动 Muxin 多协议网关演示...\n");
        
        try {
            // 创建网关
            SimpleMultiProtocolGateway gateway = new SimpleMultiProtocolGateway();
            
            // 配置网关
            configureGateway(gateway);
            
            // 启动网关
            gateway.start();
            System.out.println("✅ 网关启动成功\n");
            
            // 模拟HTTP请求
            simulateHttpRequests(gateway);
            
            // 停止网关
            gateway.stop();
            System.out.println("\n✅ 网关演示完成");
            
        } catch (Exception e) {
            System.err.println("❌ 演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 配置网关
     */
    private static void configureGateway(SimpleMultiProtocolGateway gateway) {
        System.out.println("📝 配置网关组件...");
        
        GatewayProcessor processor = gateway.getGatewayProcessor();
        
        // 配置路由
        configureRoutes(processor.getRouteManager());
        
        // 配置过滤器
        configureFilters(processor.getFilterManager());
        
        // 配置服务节点
        configureNodes(processor.getNodeManager());
        
        System.out.println("✅ 网关配置完成");
    }
    
    /**
     * 配置路由
     */
    private static void configureRoutes(RouteManager routeManager) {
        Protocol httpProtocol = new HttpProtocol();
        
        // 用户服务路由
        UniversalRoute userRoute = new DefaultRoute.Builder()
            .id("user-route")
            .name("用户服务")
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/users/**"))
            .target(new SimpleRouteTarget("user-service", 
                List.of(new HttpEndpointAddress("http://localhost:8081"))))
            .build();
        
        // 订单服务路由  
        UniversalRoute orderRoute = new DefaultRoute.Builder()
            .id("order-route")
            .name("订单服务")
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/orders/**"))
            .target(new SimpleRouteTarget("order-service",
                List.of(new HttpEndpointAddress("http://localhost:8082"))))
            .build();
        
        routeManager.addRoute(userRoute);
        routeManager.addRoute(orderRoute);
        
        System.out.println("  ✓ 用户服务路由: /api/users/**");
        System.out.println("  ✓ 订单服务路由: /api/orders/**");
    }
    
    /**
     * 配置过滤器
     */
    private static void configureFilters(FilterManager filterManager) {
        // 由于过滤器类需要更复杂的实现，这里简化处理
        System.out.println("  ✓ 认证过滤器 (预留)");
        System.out.println("  ✓ 日志过滤器 (预留)");
    }
    
    /**
     * 配置节点
     */
    private static void configureNodes(NodeManager nodeManager) {
        Protocol httpProtocol = new HttpProtocol();
        
        // 用户服务节点
        UniversalServiceNode userNode = new DefaultServiceNode(
            "user-node-1", "user-service", 
            new HttpEndpointAddress("http://localhost:8081"), httpProtocol);
        
        // 订单服务节点
        UniversalServiceNode orderNode = new DefaultServiceNode(
            "order-node-1", "order-service",
            new HttpEndpointAddress("http://localhost:8082"), httpProtocol);
        
        // 使用正确的addNode方法签名
        nodeManager.addNode("user-service", userNode);
        nodeManager.addNode("order-service", orderNode);
        
        System.out.println("  ✓ 用户服务节点: localhost:8081");
        System.out.println("  ✓ 订单服务节点: localhost:8082");
    }
    
    /**
     * 模拟HTTP请求
     */
    private static void simulateHttpRequests(SimpleMultiProtocolGateway gateway) throws Exception {
        System.out.println("📨 模拟HTTP请求处理...\n");
        
        // 模拟用户请求
        simulateRequest(gateway, "GET", "/api/users/123", "获取用户信息");
        Thread.sleep(100);
        
        simulateRequest(gateway, "POST", "/api/users", "创建用户");
        Thread.sleep(100);
        
        // 模拟订单请求
        simulateRequest(gateway, "GET", "/api/orders/456", "获取订单信息");
        Thread.sleep(100);
        
        simulateRequest(gateway, "POST", "/api/orders", "创建订单");
    }
    
    /**
     * 模拟单个请求
     */
    private static void simulateRequest(SimpleMultiProtocolGateway gateway, 
                                      String method, String path, String description) {
        try {
            System.out.println(String.format("🔄 %s %s (%s)", method, path, description));
            
            // 创建HTTP消息
            Message httpMessage = createHttpMessage(method, path);
            
            // 创建HTTP连接
            Connection httpConnection = createHttpConnection();
            
            // 处理请求
            CompletableFuture<Message> future = gateway.handleInbound(httpMessage, httpConnection);
            Message response = future.get();
            
            System.out.println(String.format("  ✅ 响应: %s\n", response.getMessageId()));
            
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestLine", method + " " + path + " HTTP/1.1");
        headers.set("Host", "localhost:8080");
        headers.set("User-Agent", "MuxinGateway/1.0");
        
        HttpBody body = new HttpBody(new byte[0]);
        
        HttpMetadata metadata = new HttpMetadata();
        metadata.setMethod(method);
        metadata.setPath(path);
        
        return new HttpMessage(messageId, MessageType.REQUEST, httpProtocol, headers, body, metadata);
    }
    
    /**
     * 创建HTTP连接
     */
    private static Connection createHttpConnection() {
        Protocol httpProtocol = new HttpProtocol();
        EndpointAddress localAddr = new HttpEndpointAddress("http://localhost:8080");
        EndpointAddress remoteAddr = new HttpEndpointAddress("http://client:0");
        return new HttpConnection(httpProtocol, localAddr, remoteAddr);
    }
    
    /**
     * 简单路由目标实现
     */
    private static class SimpleRouteTarget implements RouteTarget {
        private final String serviceName;
        private final List<EndpointAddress> endpoints;
        private final Protocol protocol;
        
        public SimpleRouteTarget(String serviceName, List<EndpointAddress> endpoints) {
            this.serviceName = serviceName;
            this.endpoints = new ArrayList<>(endpoints);
            this.protocol = new HttpProtocol();
        }
        
        @Override
        public Protocol getTargetProtocol() {
            return protocol;
        }
        
        @Override
        public List<EndpointAddress> getTargetAddresses() {
            return new ArrayList<>(endpoints);
        }
        
        @Override
        public String getLoadBalanceStrategy() {
            return "ROUND_ROBIN";
        }
        
        @Override
        public Map<String, Object> getTargetConfig() {
            return Map.of("service", serviceName, "timeout", 5000);
        }
        
        @Override
        public HealthCheckConfig getHealthCheckConfig() {
            return new SimpleHealthCheckConfig();
        }
        
        @Override
        public EndpointAddress selectTarget(UniversalRequestContext context) {
            return endpoints.isEmpty() ? null : endpoints.get(0);
        }
        
        /**
         * 简单健康检查配置
         */
        private static class SimpleHealthCheckConfig implements HealthCheckConfig {
            @Override
            public boolean isEnabled() {
                return true;
            }
            
            @Override
            public java.time.Duration getInterval() {
                return java.time.Duration.ofSeconds(30);
            }
            
            @Override
            public java.time.Duration getTimeout() {
                return java.time.Duration.ofSeconds(5);
            }
            
            @Override
            public String getPath() {
                return "/health";
            }
            
            @Override
            public List<Integer> getExpectedStatusCodes() {
                return List.of(200, 201);
            }
            
            @Override
            public int getFailureThreshold() {
                return 3;
            }
            
            @Override
            public int getSuccessThreshold() {
                return 2;
            }
        }
    }
} 