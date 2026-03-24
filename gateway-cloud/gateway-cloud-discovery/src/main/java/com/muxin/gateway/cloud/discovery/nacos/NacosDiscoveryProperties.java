package com.muxin.gateway.cloud.discovery.nacos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NacosDiscoveryProperties {

    private boolean enabled = true;

    private String group = "DEFAULT_GROUP";

    private String clusterName;

    private boolean watch = true;

    private long watchDelay = 30000L;

    private boolean cacheEnabled = true;

    private String cacheDir;

    private Map<String, String> metadata = new HashMap<>();

    private Map<String, String> serviceConfig = new HashMap<>();
}