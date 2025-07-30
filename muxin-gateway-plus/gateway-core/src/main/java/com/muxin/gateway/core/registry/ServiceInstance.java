package com.muxin.gateway.core.registry;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 服务实例接口
 * 表示一个具体的服务实例
 *
 * @author Administrator
 * @date 2025/6/13 18:00
 */
public interface ServiceInstance {

    /**
     * 获取服务定义
     *
     * @return 服务定义
     */
    ServiceDefinition getServiceDefinition();

    /**
     * 获取实例ID
     *
     * @return 实例ID
     */
    default String getInstanceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取主机地址
     *
     * @return 主机地址
     */
    String getHost();

    /**
     * 获取端口号
     *
     * @return 端口号
     */
    int getPort();

    /**
     * 是否启用HTTPS
     *
     * @return 是否安全连接
     */
    boolean isSecure();

    /**
     * 获取服务URI
     *
     * @return 服务URI
     */
    URI getUri();

    /**
     * 获取实例权重
     *
     * @return 权重值
     */
    default double getWeight() {
        return 1.0;
    }

    /**
     * 是否健康
     *
     * @return 是否健康
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * 获取实例元数据
     *
     * @return 元数据
     */
    default Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }
} 