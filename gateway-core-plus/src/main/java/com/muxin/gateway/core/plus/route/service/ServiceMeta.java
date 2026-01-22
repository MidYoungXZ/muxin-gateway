package com.muxin.gateway.core.plus.route.service;


import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;

/**
 * 服务元数据接口
 * 定义服务的元数据信息，包括服务ID、名称、版本、协议、地址等
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
