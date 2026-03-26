package com.muxin.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "muxin.gateway")
public class GatewayProperties {

    private boolean enabled = true;

    private String name = "muxin-gateway";

    private String version = "1.0.0";

    private NettyServerProperties netty = new NettyServerProperties();

    private AdminProperties admin = new AdminProperties();

    @Data
    public static class AdminProperties {
        private boolean enabled = true;
        private String pathPrefix = "/admin";
        private String username = "admin";
        private String password = "admin123";
        private int sessionTimeout = 30;
    }
}