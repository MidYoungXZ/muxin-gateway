package com.muxin.gateway.core.plus.server.http;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP服务器配置类
 * 参考NettyHttpServer的配置设计，适配refactory架构
 * 
 * @author muxin
 */
@Data
@Builder
public class HttpServerConfig {
    
    // ========== Netty线程配置 ==========
    @Builder.Default
    private int bossThreads = 1;
    
    @Builder.Default
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    @Builder.Default
    private String bossThreadName = "refactory-http-boss";
    
    @Builder.Default
    private String workerThreadName = "refactory-http-worker";
    
    // ========== Socket配置 ==========
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
    
    // ========== HTTP协议配置 ==========
    @Builder.Default
    private int maxContentLength = 65536;  // 64KB
    
    @Builder.Default
    private int maxInitialLineLength = 4096;  // 4KB
    
    @Builder.Default
    private int maxHeaderSize = 8192;  // 8KB
    
    @Builder.Default
    private int maxChunkSize = 8192;  // 8KB
    
    // ========== 压缩配置 ==========
    @Builder.Default
    private boolean compressionEnabled = false;
    
    @Builder.Default
    private int compressionLevel = 6;
    
    @Builder.Default
    private int compressionWindowBits = 15;
    
    @Builder.Default
    private int compressionMemLevel = 8;
    
    // ========== 超时配置 ==========
    @Builder.Default
    private Duration requestTimeout = Duration.ofSeconds(30);
    
    @Builder.Default
    private Duration connectionTimeout = Duration.ofSeconds(5);
    
    @Builder.Default
    private Duration idleTimeout = Duration.ofMinutes(5);
    
    // ========== 功能开关 ==========
    @Builder.Default
    private boolean enableAccessLog = true;
    
    @Builder.Default
    private boolean enableMetrics = true;
    
    @Builder.Default
    private boolean enableGracefulShutdown = true;
    
    @Builder.Default
    private Duration gracefulShutdownTimeout = Duration.ofSeconds(30);
    
    // ========== 平台相关 ==========
    @Builder.Default
    private boolean useNativeTransport = true;
    
    @Builder.Default
    private boolean usePooledAllocator = true;
    
    /**
     * 创建默认配置
     */
    public static HttpServerConfig defaultConfig() {
        return HttpServerConfig.builder().build();
    }
    
    /**
     * 从Map创建配置
     */
    public static HttpServerConfig fromMap(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return defaultConfig();
        }
        
        HttpServerConfigBuilder builder = HttpServerConfig.builder();
        
        // 线程配置
        if (configMap.containsKey("bossThreads")) {
            builder.bossThreads((Integer) configMap.get("bossThreads"));
        }
        if (configMap.containsKey("workerThreads")) {
            builder.workerThreads((Integer) configMap.get("workerThreads"));
        }
        
        // Socket配置
        if (configMap.containsKey("backlog")) {
            builder.backlog((Integer) configMap.get("backlog"));
        }
        if (configMap.containsKey("keepAlive")) {
            builder.keepAlive((Boolean) configMap.get("keepAlive"));
        }
        if (configMap.containsKey("tcpNoDelay")) {
            builder.tcpNoDelay((Boolean) configMap.get("tcpNoDelay"));
        }
        
        // HTTP配置
        if (configMap.containsKey("maxContentLength")) {
            builder.maxContentLength((Integer) configMap.get("maxContentLength"));
        }
        if (configMap.containsKey("compressionEnabled")) {
            builder.compressionEnabled((Boolean) configMap.get("compressionEnabled"));
        }
        
        // 超时配置
        if (configMap.containsKey("requestTimeout")) {
            Object timeout = configMap.get("requestTimeout");
            if (timeout instanceof Integer) {
                builder.requestTimeout(Duration.ofSeconds((Integer) timeout));
            } else if (timeout instanceof Duration) {
                builder.requestTimeout((Duration) timeout);
            }
        }
        
        return builder.build();
    }
    
    /**
     * 转换为Map
     */
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
    
    /**
     * 验证配置的合法性
     */
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
        if (requestTimeout.toMillis() < 1000) {
            throw new IllegalArgumentException("requestTimeout must be at least 1 second");
        }
    }
} 