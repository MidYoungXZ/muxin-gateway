package com.muxin.gateway.spring.boot.starter;

import com.muxin.gateway.core.plus.GatewayBootstrap;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

/**
 * 网关生命周期管理器
 * 负责与Spring生命周期同步，管理网关组件的启动和关闭
 * 
 * @author muxin
 */
@Slf4j
public class GatewayLifecycleManager implements SmartLifecycle {
    
    private final GatewayBootstrap gatewayBootstrap;
    private final GatewayConfig gatewayConfig;
    private volatile boolean running = false;
    
    public GatewayLifecycleManager(GatewayBootstrap gatewayBootstrap, GatewayConfig gatewayConfig) {
        this.gatewayBootstrap = gatewayBootstrap;
        this.gatewayConfig = gatewayConfig;
    }

    @Override
    public void start() {
        if (!running) {
            try {
                log.info("Starting Gateway components...");
                
                // 首先配置网关
                configureGateway();
                
                // 初始化网关组件
                gatewayBootstrap.init();
                
                // 启动网关服务
                gatewayBootstrap.start();
                
                running = true;
                log.info("Gateway components started successfully");
                
            } catch (Exception e) {
                log.error("Failed to start Gateway components", e);
                throw new RuntimeException("Gateway startup failed", e);
            }
        }
    }

    @Override
    public void stop() {
        if (running) {
            try {
                log.info("Stopping Gateway components...");
                
                gatewayBootstrap.shutdown();
                
                running = false;
                log.info("Gateway components stopped successfully");
                
            } catch (Exception e) {
                log.error("Failed to stop Gateway components", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 设置为较高的优先级，确保在大多数其他组件之后启动
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public boolean isAutoStartup() {
        // 自动启动
        return true;
    }

    /**
     * 配置网关组件
     */
    private void configureGateway() {
        if (gatewayConfig != null) {
            log.debug("Configuring Gateway with provided config");
            // 这里可以添加配置设置逻辑
            // 目前 GatewayBootstrap 还没有 configure 方法，所以先注释掉
            // gatewayBootstrap.configure(gatewayConfig);
        }
    }
} 