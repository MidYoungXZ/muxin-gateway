package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 服务器配置
 * 
 * @author muxin
 */
@Data
@Builder
public class ServerConfig {
    
    @Builder.Default
    private int httpPort = 8080;
    
    @Builder.Default
    private int httpsPort = 8443;
    
    @Builder.Default
    private boolean enableSsl = false;
    
    @Builder.Default
    private int backlog = 1024;
    
    @Builder.Default
    private boolean keepAlive = true;
    
    @Builder.Default
    private boolean tcpNoDelay = true;
    
    @Builder.Default
    private int bossThreads = 1;
    
    @Builder.Default
    private int workerThreads = Runtime.getRuntime().availableProcessors();
    
    @Builder.Default
    private int maxContentLength = 10 * 1024 * 1024; // 10MB
    
    @Builder.Default
    private int maxInitialLineLength = 4096;
    
    @Builder.Default
    private int maxHeaderSize = 8192;
    
    @Builder.Default
    private int maxChunkSize = 8192;
    
    @Builder.Default
    private boolean compressionEnabled = true;
    
    @Builder.Default
    private int compressionLevel = 6;
    
    @Builder.Default
    private Duration readTimeout = Duration.ofSeconds(60);
    
    @Builder.Default
    private Duration writeTimeout = Duration.ofSeconds(60);
    
    @Builder.Default
    private Duration idleTimeout = Duration.ofMinutes(5);
    
    @Builder.Default
    private int sendBufferSize = 64 * 1024;
    
    @Builder.Default
    private int receiveBufferSize = 64 * 1024;
    
    @Builder.Default
    private boolean reuseAddr = true;
    
    public static ServerConfig defaultConfig() {
        return ServerConfig.builder().build();
    }
    
    public void validate() {
        if (httpPort <= 0 || httpPort > 65535) {
            throw new IllegalArgumentException("httpPort必须在1-65535之间");
        }
        if (httpsPort <= 0 || httpsPort > 65535) {
            throw new IllegalArgumentException("httpsPort必须在1-65535之间");
        }
        if (backlog <= 0) {
            throw new IllegalArgumentException("backlog必须大于0");
        }
        if (bossThreads <= 0) {
            throw new IllegalArgumentException("bossThreads必须大于0");
        }
        if (workerThreads <= 0) {
            throw new IllegalArgumentException("workerThreads必须大于0");
        }
        if (maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength必须大于0");
        }
    }
} 