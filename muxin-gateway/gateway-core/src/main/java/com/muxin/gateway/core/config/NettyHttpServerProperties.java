package com.muxin.gateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty HTTP服务器配置属性
 */
@Data
@ConfigurationProperties(prefix = "muxin.gateway.netty.server")
public class NettyHttpServerProperties {

    private int port = 8080;

    private int eventLoopGroupBossNum = 1;

    private String eventLoopGroupBossThreadPoolName = "netty-boss-nio";

    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors() * 2;

    private String eventLoopGroupWorkerThreadPoolName = "netty-worker-nio";

    private int maxContentLength = 1024 * 1024;

    private int backlog = 8192;

    private boolean reUseAddress = true;

    private boolean tcpNoDelay = true;

    private boolean keepAlive = false;

    private int sndBuf = 1048576;

    private int rcvBuf = 1048576;

    private int soLinger;

    private int soTimeout;

    private int writeBufferHighWaterMark = 65536;

    private int writeBufferLowWaterMark = 32768;

    private int writeSpinCount = 16;

    private boolean compressionEnabled = true;

    private int compressionLevel = 6;
}
