package com.muxin.gateway.refactory.server.http;

import com.muxin.gateway.refactory.server.MessageHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP服务器演示类
 * 展示泛型设计架构的使用方式：类型安全的协议处理
 * 
 * @author muxin
 */
@Slf4j
public class HttpServerDemo {
    
    public static void main(String[] args) {
        try {
            // 1. 创建配置
            HttpServerConfig config = HttpServerConfig.builder()
                .bossThreads(1)
                .workerThreads(4)
                .backlog(1024)
                .maxContentLength(1024 * 1024)  // 1MB
                .compressionEnabled(true)
                .enableAccessLog(true)
                .build();
            
            // 2. 创建MessageHandler
            MessageHandler messageHandler = new SimpleHttpMessageHandler();
            
            // 3. 创建泛型HTTP服务器
            NettyHttpServer server = new NettyHttpServer(8080, config);
            
            // 4. 按照泛型设计：绑定MessageHandler
            server.bindMessageHandler(messageHandler);
            
            // 5. 初始化并启动服务器
            server.init();
            server.start();
            
            log.info("=====================================");
            log.info("泛型HTTP服务器启动完成！");
            log.info("端口: 8080");
            log.info("架构: 泛型协议适配器设计");
            log.info("测试URLs:");
            log.info("  健康检查: http://localhost:8080/health");
            log.info("  回显测试: http://localhost:8080/echo");
            log.info("  连接信息: http://localhost:8080/info");
            log.info("  默认页面: http://localhost:8080/");
            log.info("=====================================");
            
            // 演示泛型架构特性
            demonstrateGenericArchitecture(server);
            
            // 6. 添加优雅关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("正在关闭HTTP服务器...");
                server.shutdown();
                log.info("HTTP服务器已关闭");
            }));
            
            // 7. 保持主线程运行
            Thread.currentThread().join();
            
        } catch (Exception e) {
            log.error("HTTP服务器启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 演示泛型架构的优势
     */
    public static void demonstrateGenericArchitecture(NettyHttpServer server) {
        log.info("=== 泛型协议适配器架构演示 ===");
        
        // 1. 类型安全
        log.info("1. 类型安全：");
        log.info("   - NettyHttpServer: GenericProtocolServer<FullHttpRequest, FullHttpResponse, ChannelHandlerContext, NettyServerConnection>");
        log.info("   - HttpProtocolAdapter: ProtocolAdapter<FullHttpRequest, FullHttpResponse, ChannelHandlerContext, NettyServerConnection>");
        log.info("   - 编译期类型检查，零运行时类型转换开销");
        
        // 2. 协议无关性
        log.info("2. 协议无关性：");
        log.info("   - GenericProtocolServer: 提供协议无关的服务器模板逻辑");
        log.info("   - ProtocolAdapter: 封装所有协议特定转换逻辑");
        log.info("   - MessageHandler: 处理统一的业务逻辑，完全协议无关");
        
        // 3. 可扩展性
        log.info("3. 可扩展性：");
        log.info("   - WebSocket: ProtocolAdapter<WebSocketFrame, WebSocketFrame, ChannelHandlerContext, NettyWebSocketConnection>");
        log.info("   - gRPC: ProtocolAdapter<StreamObserver<?>, Message, ServerCall<?,?>, GrpcServerConnection>");
        log.info("   - 新协议只需实现泛型接口，无需修改服务器代码");
        
        // 4. 性能优势
        log.info("4. 性能优势：");
        log.info("   - 泛型擦除：运行时无泛型开销");
        log.info("   - 类型安全：避免运行时类型检查和转换");
        log.info("   - 内联优化：JVM可以更好地优化泛型方法调用");
        
        // 5. 架构清晰
        log.info("5. 架构清晰：");
        log.info("   - 单一职责：每个组件职责明确");
        log.info("   - 依赖清晰：泛型约束明确了组件间的依赖关系");
        log.info("   - 易于测试：可以独立测试每个泛型组件");
        
        // 6. 运行时信息
        log.info("6. 运行时信息：");
        log.info("   - 协议适配器类型: {}", server.getProtocolAdapter().getClass().getSimpleName());
        log.info("   - 支持的协议: {}", server.getSupportedProtocol().getName());
        log.info("   - 服务器状态: {}", server.isRunning() ? "运行中" : "已停止");
        
        log.info("================================");
    }
    
    /**
     * 演示架构对比
     */
    public static void demonstrateArchitectureComparison() {
        log.info("=== 架构对比：泛型 vs 传统设计 ===");
        
        log.info("传统设计问题：");
        log.info("  × Object adaptInbound(Object data) - 类型不安全");
        log.info("  × 运行时类型检查和转换 - 性能损耗");
        log.info("  × 协议特定方法散落各处 - 职责混淆");
        log.info("  × 难以扩展新协议 - 需要修改接口");
        
        log.info("泛型设计优势：");
        log.info("  ✓ Message adaptInbound(REQ request, CTX context) - 类型安全");
        log.info("  ✓ 编译期类型检查 - 零运行时开销");
        log.info("  ✓ 协议逻辑集中在ProtocolAdapter - 职责清晰");
        log.info("  ✓ 新协议只需实现泛型接口 - 完美扩展性");
        
        log.info("========================================");
    }
} 