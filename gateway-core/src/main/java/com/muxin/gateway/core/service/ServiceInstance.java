package com.muxin.gateway.core.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 统一的服务实例接口
 * 表示网关后端的一个服务实例，无论来源是静态配置还是服务发现
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ServiceInstance {

    /**
     * 获取实例唯一标识
     */
    String getInstanceId();

    /**
     * 获取服务ID
     */
    String getServiceId();

    /**
     * 获取主机地址
     */
    String getHost();

    /**
     * 获取端口号
     */
    int getPort();

    /**
     * 获取协议类型
     */
    String getScheme();

    /**
     * 获取实例权重
     */
    default double getWeight() {
        return 1.0;
    }

    /**
     * 判断实例是否健康
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * 获取实例来源
     */
    InstanceSource getSource();

    /**
     * 获取实例元数据
     */
    default Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

    /**
     * 获取实例URI
     */
    default String getUri() {
        return getScheme() + "://" + getHost() + ":" + getPort();
    }

    /**
     * 生成默认实例ID
     */
    static String generateInstanceId(String serviceId, String host, int port) {
        return serviceId + "-" + host + "-" + port + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}