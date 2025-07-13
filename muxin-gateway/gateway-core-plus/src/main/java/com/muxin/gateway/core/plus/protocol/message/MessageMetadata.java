package com.muxin.gateway.core.plus.protocol.message;


import com.muxin.gateway.core.plus.common.Attributes;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

/**
 * 消息元数据接口
 *
 * @author muxin
 */
public interface MessageMetadata extends Attributes {
    
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
} 