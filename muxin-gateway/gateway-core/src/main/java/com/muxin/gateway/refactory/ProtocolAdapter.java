package com.muxin.gateway.refactory;


import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.Map;

/**
 * 协议适配器 - 将协议特定的请求转换为通用消息
 *
 * @author muxin
 */
public interface ProtocolAdapter {
    
    /**
     * 支持的协议
     */
    Protocol getSupportedProtocol();
    
    /**
     * 将协议特定的入站数据转换为通用消息
     */
    Message adaptInbound(Object protocolSpecificData, Connection connection);
    
    /**
     * 将通用消息转换为协议特定的出站数据
     */
    Object adaptOutbound(Message message, Connection connection);
    
    /**
     * 创建协议特定的连接
     */
    Connection createConnection(EndpointAddress address, Map<String, Object> options);
    
    /**
     * 验证地址格式
     */
    boolean validateAddress(EndpointAddress address);
    
    /**
     * 获取协议特定的配置
     */
    Map<String, Object> getProtocolConfig();
} 