package com.muxin.gateway.core.plus;

import lombok.extern.slf4j.Slf4j;

/**
 * Muxin Gateway 应用启动类
 * 提供独立的Java应用启动入口，不依赖Spring Boot
 * 
 * 使用方法：
 * 1. 编译项目：mvn clean compile
 * 2. 运行网关：java -cp "target/classes:target/dependency/*" com.muxin.gateway.core.plus.GatewayApplication
 * 
 * @author muxin
 */
@Slf4j
public class GatewayApplication {
    
    public static void main(String[] args) {
        GatewayApplication app = new GatewayApplication();
        app.start();
    }
    
    /**
     * 启动网关应用
     */
    public void start() {
        try {
            log.info("🚀 Starting Muxin Gateway Application...");
            
            // 1. 创建网关引导器
            GatewayBootstrap bootstrap = new GatewayBootstrap();
            
            // 2. 初始化并启动网关
            log.info("📝 Initializing gateway components...");
            bootstrap.init();
            
            log.info("🔧 Starting gateway services...");
            bootstrap.start();
            
            // 3. 注册关闭钩子，确保优雅关闭
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("🛑 Shutting down Muxin Gateway...");
                bootstrap.shutdown();
                log.info("✅ Muxin Gateway shutdown completed");
            }));
            
            log.info("✅ Muxin Gateway started successfully");
            log.info("🌐 Gateway is ready to process requests");
            log.info("📊 HTTP Server listening on port 8080");
            
            // 4. 保持主线程运行
            Thread.currentThread().join();
            
        } catch (Exception e) {
            log.error("❌ Failed to start Muxin Gateway", e);
            System.exit(1);
        }
    }
} 