package com.muxin.gateway.core.server;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 服务器配置类
 * 参考 NettyHttpServer 的配置设计，适配 refactory 架构
 * 
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class HttpServerConfig {
    
    // ========== Netty 线程配置 ==========
    @Builder.Default
    private int bossThreads = 1;
    
    @Builder.Default
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    @Builder.Default
    private String bossThreadName = "refactory-http-boss";
    
    @Builder.Default
    private String workerThreadName = "refactory-http-worker";
    
    // ========== Socket 配置 ==========
    @Builder.Default
    private int backlog = 1024;
    
    @Builder.Default
    private boolean reuseAddr = true;
    
    @Builder.Default
    private boolean keepAlive = true;
    
    @Builder.Default
    private boolean tcpNoDelay = true;
    
    @Builder.Default
    private int sendBufferSize = 65536;
    
    @Builder.Default
    private int receiveBufferSize = 65536;
    
    @Builder.Default
    private int writeBufferLowWaterMark = 32 * 1024;
    
    @Builder.Default
    private int writeBufferHighWaterMark = 64 * 1024;
    
    // ========== HTTP 协议配置 ==========
    @Builder.Default
    private int maxContentLength = 65536;
    
    @Builder.Default
    private int maxInitialLineLength = 4096;
    
    @Builder.Default
    private int maxHeaderSize = 8192;
    
    @Builder.Default
    private int maxChunkSize = 8192;
    
    // ========== 压缩配置 ==========
    @Builder.Default
    private boolean compressionEnabled = false;
    
    @Builder.Default
    private int compressionLevel = 6;
    
    @Builder.Default
    private int compressionWindowBits = 15;
    
    @Builder.Default
    private int compressionMemLevel = 8;
    
    // ========== 超时配置（毫秒） ==========
    @Builder.Default
    private Long requestTimeout = 30000L;
    
    @Builder.Default
    private Long connectionTimeout = 5000L;
    
    @Builder.Default
    private Long idleTimeout = 300000L;
    
    // ========== 功能开关 ==========
    @Builder.Default
    private boolean enableAccessLog = true;
    
    @Builder.Default
    private boolean enableMetrics = true;
    
    @Builder.Default
    private boolean enableGracefulShutdown = true;
    
    @Builder.Default
    private Long gracefulShutdownTimeout = 30000L;
    
    // ========== 平台相关 ==========
    @Builder.Default
    private boolean useNativeTransport = true;
    
    @Builder.Default
    private boolean usePooledAllocator = true;
    
    public static HttpServerConfig defaultConfig() {
        return HttpServerConfig.builder().build();
    }
    
    public static HttpServerConfig fromMap(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return defaultConfig();
        }
        
        HttpServerConfigBuilder builder = HttpServerConfig.builder();
        
        if (configMap.containsKey("bossThreads")) {
            builder.bossThreads((Integer) configMap.get("bossThreads"));
        }
        if (configMap.containsKey("workerThreads")) {
            builder.workerThreads((Integer) configMap.get("workerThreads"));
        }
        if (configMap.containsKey("backlog")) {
            builder.backlog((Integer) configMap.get("backlog"));
        }
        if (configMap.containsKey("keepAlive")) {
            builder.keepAlive((Boolean) configMap.get("keepAlive"));
        }
        if (configMap.containsKey("tcpNoDelay")) {
            builder.tcpNoDelay((Boolean) configMap.get("tcpNoDelay"));
        }
        if (configMap.containsKey("maxContentLength")) {
            builder.maxContentLength((Integer) configMap.get("maxContentLength"));
        }
        if (configMap.containsKey("compressionEnabled")) {
            builder.compressionEnabled((Boolean) configMap.get("compressionEnabled"));
        }
        if (configMap.containsKey("requestTimeout")) {
            Object timeout = configMap.get("requestTimeout");
            if (timeout instanceof Number) {
                builder.requestTimeout(((Number) timeout).longValue());
            }
        }
        
        return builder.build();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("bossThreads", bossThreads);
        map.put("workerThreads", workerThreads);
        map.put("backlog", backlog);
        map.put("reuseAddr", reuseAddr);
        map.put("keepAlive", keepAlive);
        map.put("tcpNoDelay", tcpNoDelay);
        map.put("sendBufferSize", sendBufferSize);
        map.put("receiveBufferSize", receiveBufferSize);
        map.put("maxContentLength", maxContentLength);
        map.put("compressionEnabled", compressionEnabled);
        map.put("compressionLevel", compressionLevel);
        map.put("requestTimeout", requestTimeout);
        map.put("connectionTimeout", connectionTimeout);
        map.put("enableAccessLog", enableAccessLog);
        map.put("enableMetrics", enableMetrics);
        map.put("useNativeTransport", useNativeTransport);
        map.put("usePooledAllocator", usePooledAllocator);
        return map;
    }
    
    public void validate() {
        if (bossThreads < 1) {
            throw new IllegalArgumentException("bossThreads must be positive");
        }
        if (workerThreads < 1) {
            throw new IllegalArgumentException("workerThreads must be positive");
        }
        if (backlog < 1) {
            throw new IllegalArgumentException("backlog must be positive");
        }
        if (maxContentLength < 1024) {
            throw new IllegalArgumentException("maxContentLength must be at least 1024 bytes");
        }
        if (compressionLevel < 1 || compressionLevel > 9) {
            throw new IllegalArgumentException("compressionLevel must be between 1 and 9");
        }
        if (requestTimeout < 1000) {
            throw new IllegalArgumentException("requestTimeout must be at least 1000 ms");
        }
    }
}