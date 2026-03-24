package com.muxin.gateway.cloud.registry.nacos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NacosRegistryProperties {

    private String serverAddr = "127.0.0.1:8848";

    private String namespace = "";

    private String group = "DEFAULT_GROUP";

    private String username;

    private String password;

    private String accessKey;

    private String secretKey;

    private String contextPath;

    private String clusterName;

    private String namingLoadCacheAtStart = "true";

    private String namingCacheRegistryDir;

    private String logName;

    private Map<String, String> metadata = new HashMap<>();

    private boolean enabled = true;

    private boolean registerEnabled = true;

    private String serviceId;

    private String serviceName;

    private String ip;

    private int port = 8080;

    private double weight = 1.0;

    private String cluster;

    private Map<String, String> serviceMetadata = new HashMap<>();

    private boolean ephemeral = true;

    public String getServiceName() {
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName;
        }
        return serviceId;
    }
}