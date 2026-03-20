package com.muxin.gateway.core.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认服务实例实现
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServiceInstance implements ServiceInstance {

    private String instanceId;
    private String serviceId;
    private String host;
    private int port;
    private String scheme;
    private double weight;
    private boolean healthy;
    private InstanceSource source;
    private Map<String, String> metadata;

    @Override
    public String getInstanceId() {
        if (instanceId == null || instanceId.isEmpty()) {
            return ServiceInstance.generateInstanceId(serviceId, host, port);
        }
        return instanceId;
    }

    @Override
    public String getScheme() {
        return scheme != null ? scheme : "http";
    }

    @Override
    public double getWeight() {
        return weight > 0 ? weight : 1.0;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata != null ? metadata : new HashMap<>();
    }

    /**
     * 创建静态配置实例
     */
    public static DefaultServiceInstance createStatic(String serviceId, String host, int port) {
        return DefaultServiceInstance.builder()
                .serviceId(serviceId)
                .host(host)
                .port(port)
                .scheme("http")
                .weight(1.0)
                .healthy(true)
                .source(InstanceSource.STATIC)
                .build();
    }

    /**
     * 创建静态配置实例（带权重）
     */
    public static DefaultServiceInstance createStatic(String serviceId, String host, int port, String scheme, double weight) {
        return DefaultServiceInstance.builder()
                .serviceId(serviceId)
                .host(host)
                .port(port)
                .scheme(scheme)
                .weight(weight)
                .healthy(true)
                .source(InstanceSource.STATIC)
                .build();
    }

    /**
     * 创建服务发现实例
     */
    public static DefaultServiceInstance createDiscovery(String serviceId, String host, int port, String scheme) {
        return DefaultServiceInstance.builder()
                .serviceId(serviceId)
                .host(host)
                .port(port)
                .scheme(scheme)
                .weight(1.0)
                .healthy(true)
                .source(InstanceSource.DISCOVERY)
                .build();
    }
}