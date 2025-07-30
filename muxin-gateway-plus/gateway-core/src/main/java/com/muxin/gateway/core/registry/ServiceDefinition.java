package com.muxin.gateway.core.registry;

import java.util.Map;

/**
 * 服务定义接口
 * 定义了服务的基本信息
 *
 * @author Administrator
 * @date 2025/6/13 18:00
 */
public interface ServiceDefinition {

    /**
     * 获取服务ID
     *
     * @return 服务ID
     */
    String getServiceId();

    /**
     * 获取服务协议
     *
     * @return 协议 (http, https等)
     */
    String getScheme();

    /**
     * 获取服务版本
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * 获取服务作用域
     *
     * @return 作用域
     */
    String getScope();

    /**
     * 服务是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 获取服务元数据
     *
     * @return 元数据Map
     */
    Map<String, String> getMetadata();

    /**
     * 获取服务描述
     *
     * @return 服务描述
     */
    default String getDescription() {
        return "";
    }

    /**
     * 获取服务环境
     *
     * @return 环境标识 (dev, test, prod等)
     */
    default String getEnvironment() {
        return "prod";
    }
} 