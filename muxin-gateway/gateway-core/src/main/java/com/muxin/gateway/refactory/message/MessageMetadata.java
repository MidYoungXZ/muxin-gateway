package com.muxin.gateway.refactory.message;


import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.Map;

/**
 * 消息元数据接口
 *
 * @author muxin
 */
public interface MessageMetadata {
    
    /**
     * 消息时间戳
     */
    long getTimestamp();
    
    /**
     * 接收时间
     */
    long getReceiveTime();
    
    /**
     * 发送时间
     */
    long getSendTime();
    
    /**
     * 来源地址
     */
    EndpointAddress getSourceAddress();
    
    /**
     * 目标地址
     */
    EndpointAddress getTargetAddress();
    
    /**
     * 连接ID
     */
    String getConnectionId();
    
    /**
     * 路由ID
     */
    String getRouteId();
    
    /**
     * 服务名称
     */
    String getServiceName();
    
    /**
     * 追踪ID
     */
    String getTraceId();
    
    /**
     * Span ID
     */
    String getSpanId();
    
    /**
     * 扩展属性
     */
    Map<String, Object> getAttributes();
    
    /**
     * 获取属性
     */
    <T> T getAttribute(String key, Class<T> type);
    
    /**
     * 设置属性
     */
    void setAttribute(String key, Object value);
} 