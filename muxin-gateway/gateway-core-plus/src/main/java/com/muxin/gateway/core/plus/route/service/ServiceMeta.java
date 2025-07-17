package com.muxin.gateway.core.plus.route.service;


import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;

/**
 * @author: yangxz
 * @description:
 */
public interface ServiceMeta {
    /**
     * 服务唯一标识
     */
    String getServiceId();

    /**
     * 服务名称
     */
    String getServiceName();

    /**
     * 服务版本
     */
    String getVersion();

    /**
     * 服务描述
     */
    String getDescription();

    /**
     * 获取节点元数据
     */
    Map<String, Object> getMetadata();

    /**
     * 服务支持的协议
     */
    Protocol getProtocol();


}
