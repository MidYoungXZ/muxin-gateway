package com.muxin.gateway.registry.nacos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 服务发现配置
 * 用于配置从 Nacos 发现服务的行为
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
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