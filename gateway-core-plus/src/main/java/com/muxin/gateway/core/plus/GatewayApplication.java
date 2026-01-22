package com.muxin.gateway.core.plus;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关应用程序主类
 * 
 * Spring Boot应用程序启动入口，负责初始化网关核心组件、
 * 配置自动装配，并启动Netty服务器和管理界面
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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