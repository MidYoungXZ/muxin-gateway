package com.muxin.gateway.core.service;

import java.util.Map;

/**
 * HTTP服务元数据接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ServiceMeta {

    String getServiceId();

    String getServiceName();

    String getVersion();

    String getDescription();

    Map<String, Object> getMetadata();

    String getScheme();
}
