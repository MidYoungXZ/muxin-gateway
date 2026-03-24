package com.muxin.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "muxin.gateway")
public class GatewayProperties {

    private boolean enabled = true;

    private String name = "muxin-gateway";

    private String version = "1.0.0";

    private NettyServerProperties netty = new NettyServerProperties();

    private NacosRegistryProperties registry = new NacosRegistryProperties();

    private AdminProperties admin = new AdminProperties();

    @Data
    public static class NacosRegistryProperties {
        private boolean enabled = true;
        private String serverAddr = "127.0.0.1:8848";
        private String namespace = "";
        private String group = "DEFAULT_GROUP";
        private String username;
        private String password;
        private boolean registerEnabled = true;
        private String serviceName;
        private int port = 8081;
        private double weight = 1.0;
        private Map<String, String> metadata = new HashMap<>();
    }

    @Data
    public static class AdminProperties {
        private boolean enabled = true;
        private String pathPrefix = "/admin";
        private String username = "admin";
        private String password = "admin123";
        private int sessionTimeout = 30;
    }
}