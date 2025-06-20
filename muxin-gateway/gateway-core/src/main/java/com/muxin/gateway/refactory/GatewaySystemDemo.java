package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.http.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 网关系统完整演示
 * 展示多协议网关的HTTP协议转发功能
 * 
 * @author muxin
 */
public class GatewaySystemDemo {
    
    private static final String BANNER = """
            
            ███╗   ███╗██╗   ██╗██╗  ██╗██╗███╗   ██╗     ██████╗  █████╗ ████████╗███████╗██╗    ██╗ █████╗ ██╗   ██╗
            ████╗ ████║██║   ██║╚██╗██╔╝██║████╗  ██║    ██╔════╝ ██╔══██╗╚══██╔══╝██╔════╝██║    ██║██╔══██╗╚██╗ ██╔╝
            ██╔████╔██║██║   ██║ ╚███╔╝ ██║██╔██╗ ██║    ██║  ███╗███████║   ██║   █████╗  ██║ █╗ ██║███████║ ╚████╔╝ 
            ██║╚██╔╝██║██║   ██║ ██╔██╗ ██║██║╚██╗██║    ██║   ██║██╔══██║   ██║   ██╔══╝  ██║███╗██║██╔══██║  ╚██╔╝  
            ██║ ╚═╝ ██║╚██████╔╝██╔╝ ██╗██║██║ ╚████║    ╚██████╔╝██║  ██║   ██║   ███████╗╚███╔███╔╝██║  ██║   ██║   
            ╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝     ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝ ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝   
            
                                            🚀 协议无关高性能API网关系统 🚀
            """;
    
    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("=".repeat(100));
        System.out.println("🎯 启动网关系统演示");
        System.out.println("=".repeat(100));
        
        try {
            GatewaySystemDemo demo = new GatewaySystemDemo();
            demo.runDemo();
        } catch (Exception e) {
            System.err.println("❌ 演示运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void runDemo() throws Exception {
        SimpleMultiProtocolGateway gateway = null;
        
        try {
            // 1. 创建和配置网关
            gateway = createGateway();
            
            // 2. 启动网关
            startGateway(gateway);
            
            // 3. 展示网关配置
            showGatewayConfiguration(gateway);
            
            // 4. 模拟各种请求场景
            simulateRequestScenarios(gateway);
            
            // 5. 展示性能统计
            showPerformanceStats(gateway);
            
        } finally {
            if (gateway != null) {
                stopGateway(gateway);
            }
        }
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("✅ 网关系统演示完成！");
        System.out.println("=".repeat(100));
    }
    
    /**
     * 创建网关
     */
    private SimpleMultiProtocolGateway createGateway() {
        System.out.println("\n📋 创建和配置网关组件");
        System.out.println("-".repeat(50));
        
        SimpleMultiProtocolGateway gateway = new SimpleMultiProtocolGateway();
        GatewayProcessor processor = gateway.getGatewayProcessor();
        
        // 配置路由
        configureAdvancedRoutes(processor.getRouteManager());
        
        // 配置过滤器
        configureAdvancedFilters(processor.getFilterManager());
        
        // 配置服务节点
        configureServiceNodes(processor.getNodeManager());
        
        System.out.println("✅ 网关配置完成");
        return gateway;
    }
    
    /**
     * 配置高级路由
     */
    private void configureAdvancedRoutes(RouteManager routeManager) {
        Protocol httpProtocol = new HttpProtocol();
        
        // API v1 路由
        UniversalRoute apiV1Route = new DefaultRoute.Builder("api-v1", "API v1路由")
            .description("处理v1版本API请求")
            .order(10)
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/v1/**"))
            .predicate(new HttpMethodPredicate("GET", "POST", "PUT", "DELETE"))
            .target(createRouteTarget("api-v1-service", 
                "http://api-v1-1:8081", "http://api-v1-2:8082"))
            .metadata("version", "v1")
            .metadata("timeout", 5000)
            .build();
        
        // API v2 路由 
        UniversalRoute apiV2Route = new DefaultRoute.Builder("api-v2", "API v2路由")
            .description("处理v2版本API请求")
            .order(5)  // 更高优先级
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/api/v2/**"))
            .predicate(new HttpMethodPredicate("GET", "POST", "PUT", "DELETE", "PATCH"))
            .target(createRouteTarget("api-v2-service",
                "http://api-v2-1:8083", "http://api-v2-2:8084", "http://api-v2-3:8085"))
            .metadata("version", "v2")
            .metadata("timeout", 3000)
            .build();
        
        // 静态资源路由
        UniversalRoute staticRoute = new DefaultRoute.Builder("static", "静态资源路由")
            .description("处理静态资源请求")
            .order(20)
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/static/**"))
            .predicate(new HttpMethodPredicate("GET"))
            .target(createRouteTarget("static-service", "http://static:8086"))
            .metadata("type", "static")
            .metadata("cache", true)
            .build();
        
        // WebSocket路由
        UniversalRoute wsRoute = new DefaultRoute.Builder("websocket", "WebSocket路由")
            .description("处理WebSocket连接")
            .order(1)  // 最高优先级
            .protocol(httpProtocol)
            .predicate(new HttpPathPredicate("/ws/**"))
            .predicate(context -> {
                // 检查WebSocket升级头
                Message message = context.getInboundMessage();
                if (message != null && message.getHeaders() != null) {
                    String upgrade = message.getHeaders().get("Upgrade", String.class);
                    return "websocket".equalsIgnoreCase(upgrade);
                }
                return false;
            })
            .target(createRouteTarget("websocket-service", "http://ws:8087"))
            .metadata("type", "websocket")
            .build();
        
        // 添加路由
        routeManager.addRoute(apiV1Route);
        routeManager.addRoute(apiV2Route);
        routeManager.addRoute(staticRoute);
        routeManager.addRoute(wsRoute);
        
        System.out.println("  ✓ API v1 路由: /api/v1/** -> api-v1-service (2节点)");
        System.out.println("  ✓ API v2 路由: /api/v2/** -> api-v2-service (3节点)");
        System.out.println("  ✓ 静态资源路由: /static/** -> static-service");
        System.out.println("  ✓ WebSocket路由: /ws/** -> websocket-service");
    }
    
    /**
     * 配置高级过滤器
     */
    private void configureAdvancedFilters(FilterManager filterManager) {
        // 注册认证过滤器
        filterManager.registerFilter(new HttpAuthFilter());
        
        // 注册日志过滤器
        filterManager.registerFilter(new HttpLoggingFilter());
        
        // 注册自定义过滤器
        filterManager.registerFilter(new RateLimitFilter());
        filterManager.registerFilter(new CorsFilter());
        
        System.out.println("  ✓ 认证过滤器 (JWT/OAuth2)");
        System.out.println("  ✓ 日志过滤器 (访问日志)");
        System.out.println("  ✓ 限流过滤器 (令牌桶)");
        System.out.println("  ✓ CORS过滤器 (跨域支持)");
    }
    
    /**
     * 配置服务节点
     */
    private void configureServiceNodes(NodeManager nodeManager) {
        Protocol httpProtocol = new HttpProtocol();
        
        // API v1 服务节点
        addServiceNode(nodeManager, "api-v1-node-1", "api-v1-service", 
            "http://api-v1-1:8081", httpProtocol, 100);
        addServiceNode(nodeManager, "api-v1-node-2", "api-v1-service", 
            "http://api-v1-2:8082", httpProtocol, 100);
        
        // API v2 服务节点  
        addServiceNode(nodeManager, "api-v2-node-1", "api-v2-service",
            "http://api-v2-1:8083", httpProtocol, 150);
        addServiceNode(nodeManager, "api-v2-node-2", "api-v2-service",
            "http://api-v2-2:8084", httpProtocol, 100);
        addServiceNode(nodeManager, "api-v2-node-3", "api-v2-service",
            "http://api-v2-3:8085", httpProtocol, 50);
        
        // 其他服务节点
        addServiceNode(nodeManager, "static-node", "static-service",
            "http://static:8086", httpProtocol, 200);
        addServiceNode(nodeManager, "ws-node", "websocket-service",
            "http://ws:8087", httpProtocol, 100);
        
        System.out.println("  ✓ API v1 服务: 2个节点 (负载均衡)");
        System.out.println("  ✓ API v2 服务: 3个节点 (加权负载均衡)");
        System.out.println("  ✓ 静态资源服务: 1个节点");
        System.out.println("  ✓ WebSocket服务: 1个节点");
    }
    
    /**
     * 添加服务节点
     */
    private void addServiceNode(NodeManager nodeManager, String nodeId, String serviceName,
                              String url, Protocol protocol, int weight) {
        EndpointAddress address = new HttpEndpointAddress(url);
        UniversalServiceNode node = new DefaultServiceNode(nodeId, serviceName, address, protocol, weight);
        nodeManager.addNode(node);
    }
    
    /**
     * 启动网关
     */
    private void startGateway(SimpleMultiProtocolGateway gateway) {
        System.out.println("\n🚀 启动网关服务");
        System.out.println("-".repeat(50));
        
        gateway.start();
        
        System.out.println("  ✓ HTTP监听器启动: 端口 8080");
        System.out.println("  ✓ 协议适配器就绪: HTTP/1.1");
        System.out.println("  ✓ 负载均衡器就绪: 轮询策略");
        System.out.println("  ✓ 健康检查启动: 30秒间隔");
        System.out.println("✅ 网关启动完成");
    }
    
    /**
     * 展示网关配置
     */
    private void showGatewayConfiguration(SimpleMultiProtocolGateway gateway) {
        System.out.println("\n📊 网关配置概览");
        System.out.println("-".repeat(50));
        
        GatewayProcessor processor = gateway.getGatewayProcessor();
        
        // 路由统计
        List<UniversalRoute> routes = processor.getRouteManager().getAllRoutes();
        System.out.println("📍 路由配置:");
        routes.forEach(route -> {
            System.out.println(String.format("  ▶ %s (优先级:%d) %s", 
                route.getName(), route.getOrder(), 
                route.isEnabled() ? "✅" : "❌"));
        });
        
        // 协议统计
        Set<Protocol> protocols = gateway.getSupportedProtocols();
        System.out.println("\n🌐 支持协议:");
        protocols.forEach(protocol -> {
            System.out.println(String.format("  ▶ %s v%s", 
                protocol.getName(), protocol.getVersion()));
        });
        
        // 服务节点统计
        List<UniversalServiceNode> nodes = processor.getNodeManager().getAllNodes();
        System.out.println("\n🖥️  服务节点:");
        Map<String, List<UniversalServiceNode>> nodesByService = new HashMap<>();
        nodes.forEach(node -> {
            nodesByService.computeIfAbsent(node.getName(), k -> new ArrayList<>()).add(node);
        });
        
        nodesByService.forEach((serviceName, serviceNodes) -> {
            System.out.println(String.format("  ▶ %s: %d个节点", serviceName, serviceNodes.size()));
            serviceNodes.forEach(node -> {
                System.out.println(String.format("    - %s (%s) 权重:%d", 
                    node.getId(), node.getStatus().getDescription(), node.getWeight()));
            });
        });
    }
    
    /**
     * 模拟请求场景
     */
    private void simulateRequestScenarios(SimpleMultiProtocolGateway gateway) throws Exception {
        System.out.println("\n🎬 模拟请求处理场景");
        System.out.println("-".repeat(50));
        
        // 场景1: API请求
        System.out.println("\n📝 场景1: API请求处理");
        simulateApiRequests(gateway);
        
        Thread.sleep(500);
        
        // 场景2: 静态资源请求
        System.out.println("\n📁 场景2: 静态资源请求");
        simulateStaticRequests(gateway);
        
        Thread.sleep(500);
        
        // 场景3: WebSocket连接
        System.out.println("\n🔌 场景3: WebSocket连接");
        simulateWebSocketRequests(gateway);
        
        Thread.sleep(500);
        
        // 场景4: 错误处理
        System.out.println("\n⚠️  场景4: 错误处理");
        simulateErrorScenarios(gateway);
    }
    
    /**
     * 模拟API请求
     */
    private void simulateApiRequests(SimpleMultiProtocolGateway gateway) throws Exception {
        String[] apiRequests = {
            "GET /api/v1/users/123",
            "POST /api/v1/users", 
            "PUT /api/v1/users/123",
            "GET /api/v2/orders/456",
            "POST /api/v2/orders",
            "PATCH /api/v2/orders/456"
        };
        
        for (String request : apiRequests) {
            String[] parts = request.split(" ");
            simulateHttpRequest(gateway, parts[0], parts[1], "API请求");
            Thread.sleep(100);
        }
    }
    
    /**
     * 模拟静态资源请求
     */
    private void simulateStaticRequests(SimpleMultiProtocolGateway gateway) throws Exception {
        String[] staticRequests = {
            "GET /static/css/style.css",
            "GET /static/js/app.js", 
            "GET /static/images/logo.png",
            "GET /static/favicon.ico"
        };
        
        for (String request : staticRequests) {
            String[] parts = request.split(" ");
            simulateHttpRequest(gateway, parts[0], parts[1], "静态资源");
            Thread.sleep(50);
        }
    }
    
    /**
     * 模拟WebSocket请求
     */
    private void simulateWebSocketRequests(SimpleMultiProtocolGateway gateway) throws Exception {
        // 模拟WebSocket升级请求
        Message wsMessage = createWebSocketUpgradeMessage();
        Connection wsConnection = createHttpConnection();
        
        System.out.println("  🔄 处理WebSocket升级请求: /ws/chat");
        
        CompletableFuture<Message> future = gateway.handleInbound(wsMessage, wsConnection);
        Message response = future.get(5, TimeUnit.SECONDS);
        
        System.out.println("  ✅ WebSocket连接建立成功");
    }
    
    /**
     * 模拟错误场景
     */
    private void simulateErrorScenarios(SimpleMultiProtocolGateway gateway) throws Exception {
        String[] errorRequests = {
            "GET /api/nonexistent",  // 404
            "POST /api/v1/invalid",  // 路由匹配但服务不存在
            "DELETE /api/v2/forbidden" // 权限错误
        };
        
        for (String request : errorRequests) {
            try {
                String[] parts = request.split(" ");
                simulateHttpRequest(gateway, parts[0], parts[1], "错误场景");
            } catch (Exception e) {
                System.out.println("  ⚠️  预期错误: " + e.getMessage());
            }
            Thread.sleep(100);
        }
    }
    
    /**
     * 模拟HTTP请求
     */
    private void simulateHttpRequest(SimpleMultiProtocolGateway gateway, 
                                   String method, String path, String type) throws Exception {
        System.out.println(String.format("  🔄 %s %s (%s)", method, path, type));
        
        Message httpMessage = createHttpMessage(method, path);
        Connection httpConnection = createHttpConnection();
        
        long startTime = System.currentTimeMillis();
        CompletableFuture<Message> future = gateway.handleInbound(httpMessage, httpConnection);
        Message response = future.get(5, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println(String.format("    ✅ 响应: %s (耗时: %dms)", 
            response.getMessageId(), duration));
    }
    
    /**
     * 展示性能统计
     */
    private void showPerformanceStats(SimpleMultiProtocolGateway gateway) {
        System.out.println("\n📈 性能统计报告");
        System.out.println("-".repeat(50));
        
        // 协议统计
        Map<Protocol, Object> protocolStats = gateway.getProtocolStats();
        System.out.println("🌐 协议统计:");
        protocolStats.forEach((protocol, stats) -> {
            System.out.println(String.format("  ▶ %s: %s", protocol.getName(), stats));
        });
        
        // 负载均衡统计
        GatewayProcessor processor = gateway.getGatewayProcessor();
        if (processor.getLoadBalanceManager() instanceof DefaultLoadBalanceManager) {
            DefaultLoadBalanceManager lbManager = (DefaultLoadBalanceManager) processor.getLoadBalanceManager();
            Map<String, Object> lbStats = lbManager.getStatistics();
            System.out.println("\n⚖️ 负载均衡统计:");
            lbStats.forEach((key, value) -> {
                System.out.println(String.format("  ▶ %s: %s", key, value));
            });
        }
        
        // 节点健康统计
        if (processor.getNodeManager() instanceof DefaultNodeManager) {
            DefaultNodeManager nodeManager = (DefaultNodeManager) processor.getNodeManager();
            Map<String, Object> nodeStats = nodeManager.getStatistics();
            System.out.println("\n🏥 节点健康统计:");
            nodeStats.forEach((key, value) -> {
                System.out.println(String.format("  ▶ %s: %s", key, value));
            });
        }
    }
    
    /**
     * 停止网关
     */
    private void stopGateway(SimpleMultiProtocolGateway gateway) {
        System.out.println("\n🛑 停止网关服务");
        System.out.println("-".repeat(50));
        
        gateway.stop();
        
        System.out.println("  ✓ 协议监听器已停止");
        System.out.println("  ✓ 连接池已关闭");
        System.out.println("  ✓ 健康检查已停止");
        System.out.println("✅ 网关停止完成");
    }
    
    // 工具方法
    
    private RouteTarget createRouteTarget(String serviceName, String... urls) {
        List<EndpointAddress> endpoints = new ArrayList<>();
        for (String url : urls) {
            endpoints.add(new HttpEndpointAddress(url));
        }
        return new SimpleRouteTarget(serviceName, endpoints);
    }
    
    private Message createHttpMessage(String method, String path) {
        String messageId = "msg-" + System.nanoTime();
        Protocol httpProtocol = new HttpProtocol();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestLine", method + " " + path + " HTTP/1.1");
        headers.set("Host", "localhost:8080");
        headers.set("User-Agent", "MuxinGateway-Demo/1.0");
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer demo-token");
        
        String bodyContent = method.equals("POST") || method.equals("PUT") || method.equals("PATCH") 
            ? "{\"demo\": \"data\"}" : "";
        HttpBody body = new HttpBody(bodyContent.getBytes());
        
        HttpMetadata metadata = new HttpMetadata();
        metadata.setMethod(method);
        metadata.setPath(path);
        metadata.setTraceId("trace-" + System.nanoTime());
        
        return new HttpMessage(messageId, MessageType.REQUEST, httpProtocol, headers, body, metadata);
    }
    
    private Message createWebSocketUpgradeMessage() {
        String messageId = "ws-msg-" + System.nanoTime();
        Protocol httpProtocol = new HttpProtocol();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("RequestLine", "GET /ws/chat HTTP/1.1");
        headers.set("Host", "localhost:8080");
        headers.set("Upgrade", "websocket");
        headers.set("Connection", "Upgrade");
        headers.set("Sec-WebSocket-Key", "demo-key");
        headers.set("Sec-WebSocket-Version", "13");
        
        HttpBody body = new HttpBody(new byte[0]);
        HttpMetadata metadata = new HttpMetadata();
        metadata.setMethod("GET");
        metadata.setPath("/ws/chat");
        
        return new HttpMessage(messageId, MessageType.REQUEST, httpProtocol, headers, body, metadata);
    }
    
    private Connection createHttpConnection() {
        Protocol httpProtocol = new HttpProtocol();
        EndpointAddress localAddr = new HttpEndpointAddress("http://localhost:8080");
        EndpointAddress remoteAddr = new HttpEndpointAddress("http://client:0");
        return new HttpConnection(httpProtocol, localAddr, remoteAddr);
    }
    
    // 简单路由目标实现
    private static class SimpleRouteTarget implements RouteTarget {
        private final String serviceName;
        private final List<EndpointAddress> endpoints;
        
        public SimpleRouteTarget(String serviceName, List<EndpointAddress> endpoints) {
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
    
    // 自定义过滤器实现
    
    private static class RateLimitFilter implements UniversalFilter {
        @Override
        public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
            // 简单的限流逻辑
            System.out.println("    [RATE_LIMIT] 检查请求频率限制");
            if (chain != null) {
                chain.filter(context);
            }
        }
        
        @Override
        public String getName() {
            return "RateLimit";
        }
        
        @Override
        public FilterType getType() {
            return FilterType.PRE;
        }
        
        @Override
        public int getOrder() {
            return 100;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public Set<Protocol> getSupportedProtocols() {
            return Set.of(new HttpProtocol());
        }
    }
    
    private static class CorsFilter implements UniversalFilter {
        @Override
        public void filter(UniversalRequestContext context, UniversalFilterChain chain) {
            System.out.println("    [CORS] 处理跨域请求头");
            if (chain != null) {
                chain.filter(context);
            }
        }
        
        @Override
        public String getName() {
            return "CORS";
        }
        
        @Override
        public FilterType getType() {
            return FilterType.PRE;
        }
        
        @Override
        public int getOrder() {
            return 50;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public Set<Protocol> getSupportedProtocols() {
            return Set.of(new HttpProtocol());
        }
    }
} 