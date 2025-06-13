package com.muxin.gateway.core;

import com.muxin.gateway.core.config.GatewayAutoConfiguration;
import com.muxin.gateway.core.netty.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(GatewayAutoConfiguration.class)
@Slf4j
public class MuxinGatewayApplication implements CommandLineRunner {

    @Autowired
    private NettyHttpServer nettyHttpServer;

    public static void main(String[] args) {
        SpringApplication.run(MuxinGatewayApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            nettyHttpServer.start();
            log.info("=== Muxin Gateway started successfully! ===");
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down Muxin Gateway...");
                nettyHttpServer.shutdown();
            }));
            
        } catch (Exception e) {
            log.error("Failed to start Muxin Gateway", e);
            System.exit(1);
        }
    }
}
