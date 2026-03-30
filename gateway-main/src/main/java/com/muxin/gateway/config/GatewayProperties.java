package com.muxin.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "muxin.gateway")
public class GatewayProperties {

    private boolean enabled = true;

    private String name = "muxin-gateway";

    private String version = "1.0.0";

    private NettyProperties netty = new NettyProperties();

    private AdminProperties admin = new AdminProperties();

    @Data
    public static class NettyProperties {
        private ServerProperties server = new ServerProperties();
    }

    @Data
    public static class ServerProperties {
        private int port = 9292;
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

    @Data
    public static class AdminProperties {
        private boolean enabled = true;
        private String pathPrefix = "/admin";
        private String username = "admin";
        private String password = "admin123";
        private int sessionTimeout = 30;
    }

    public NettyProperties getNetty() {
        return netty;
    }
}