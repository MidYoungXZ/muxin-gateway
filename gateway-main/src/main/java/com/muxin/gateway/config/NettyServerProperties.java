package com.muxin.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "muxin.gateway.netty.server")
public class NettyServerProperties {

    private int port = 8081;

    private int bossThreads = 1;

    private int workerThreads = 4;

    private String bossThreadName = "gateway-boss";

    private String workerThreadName = "gateway-worker";

    private int backlog = 1024;

    private boolean reuseAddress = true;

    private boolean tcpNoDelay = true;

    private boolean keepAlive = true;

    private int sndBuf = 65535;

    private int rcvBuf = 65535;

    private long maxContentLength = 67108864L;

    private long requestTimeout = 30000L;

    private long connectionTimeout = 5000L;

    private long idleTimeout = 300000L;

    private boolean compressionEnabled = false;
}