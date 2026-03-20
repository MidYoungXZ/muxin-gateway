package com.muxin.gateway.core.service;

import java.util.Map;

/**
 * HTTP端点地址接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface EndpointAddress {

    String getHost();

    int getPort();

    String getPath();

    Map<String, String> getParameters();

    String toUri();

    boolean isValid();

    Map<String, Object> getMetadata();

    String getScheme();
}
