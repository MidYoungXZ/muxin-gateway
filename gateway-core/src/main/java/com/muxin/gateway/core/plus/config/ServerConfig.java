package com.muxin.gateway.core.plus.config;

import lombok.Builder;
import lombok.Data;

/**
 * 服务器配置
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
    private int maxContentLength = 10 * 1024 * 1024;
    
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
    private Long readTimeout = 60000L;
    
    @Builder.Default
    private Long writeTimeout = 60000L;
    
    @Builder.Default
    private Long idleTimeout = 300000L;
    
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
            throw new IllegalArgumentException("httpPort 必须在 1-65535 之间");
        }
        if (httpsPort <= 0 || httpsPort > 65535) {
            throw new IllegalArgumentException("httpsPort 必须在 1-65535 之间");
        }
        if (backlog <= 0) {
            throw new IllegalArgumentException("backlog 必须大于 0");
        }
        if (bossThreads <= 0) {
            throw new IllegalArgumentException("bossThreads 必须大于 0");
        }
        if (workerThreads <= 0) {
            throw new IllegalArgumentException("workerThreads 必须大于 0");
        }
        if (maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength 必须大于 0");
        }
    }
}
