package com.muxin.gateway.core.plus.route.service;

import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;

/**
 * 端点地址接口 - 协议无关的地址抽象
 *
 * @author muxin
 */
public interface EndpointAddress {
    
    /**
     * 协议类型
     */
    Protocol getProtocol();
    
    /**
     * 主机地址
     */
    String getHost();
    
    /**
     * 端口号
     */
    int getPort();
    
    /**
     * 路径或资源标识
     */
    String getPath();
    
    /**
     * 查询参数
     */
    Map<String, String> getParameters();
    
    /**
     * 完整URI
     */
    String toUri();
    
    /**
     * 地址是否有效
     */
    boolean isValid();
    
    /**
     * 协议特定的地址信息
     */
    Map<String, Object> getProtocolSpecificInfo();
} 