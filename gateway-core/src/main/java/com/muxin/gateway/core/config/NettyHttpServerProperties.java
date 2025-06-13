package com.muxin.gateway.core.config;

import lombok.Data;

/**
 * Netty HTTP服务器配置属性
 */
@Data
public class NettyHttpServerProperties {

    private int port = 8080;

    private int eventLoopGroupBossNum = 1;

    private String eventLoopGroupBossThreadPoolName = "netty-boss-nio";

    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors();

    private String eventLoopGroupWorkerThreadPoolName = "netty-worker-nio";

    private int maxContentLength = 64 * 1024 * 1024;

    private int backlog = 1024;

    private boolean reUseAddress = true;

    private boolean tcpNoDelay = true;

    private boolean keepAlive = true;

    private int sndBuf = 65535;

    private int rcvBuf = 65535;

    private int soLinger;

    private int soTimeout;
}
