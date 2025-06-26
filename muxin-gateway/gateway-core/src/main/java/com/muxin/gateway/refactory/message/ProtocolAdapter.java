package com.muxin.gateway.refactory.message;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.Map;

/**
 * 泛型协议适配器接口
 * 支持类型安全的协议转换，提供完整的协议无关性
 * 
 * @param <REQ> 协议特定的请求类型
 * @param <RESP> 协议特定的响应类型  
 * @param <CTX> 协议特定的上下文类型
 * @param <CONN> 协议特定的连接类型
 * 
 * @author muxin
 */
public interface ProtocolAdapter<REQ, RESP, CTX, CONN extends Connection> {
    
    /**
     * 支持的协议
     */
    Protocol getSupportedProtocol();
    
    /**
     * 入站适配：协议特定请求 -> 统一消息
     * 
     * @param request 协议特定的请求对象
     * @param context 协议特定的上下文对象
     * @return 统一的消息对象
     */
    Message adaptInbound(REQ request, CTX context);
    
    /**
     * 出站适配：统一消息 -> 协议特定响应
     * 
     * @param message 统一的消息对象
     * @param context 协议特定的上下文对象
     * @return 协议特定的响应对象
     */
    RESP adaptOutbound(Message message, CTX context);
    
    /**
     * 创建服务器端连接
     * 
     * @param context 协议特定的上下文对象
     * @return 协议特定的连接对象
     */
    CONN createServerConnection(CTX context);
    
    /**
     * 创建客户端连接
     * 
     * @param address 目标地址
     * @param options 连接选项
     * @return 协议特定的连接对象
     */
    CONN createClientConnection(EndpointAddress address, Map<String, Object> options);
    
    /**
     * 创建错误响应消息
     * 
     * @param statusCode 状态码
     * @param message 错误消息
     * @return 统一的错误响应消息
     */
    Message createErrorResponse(int statusCode, String message);
    
    /**
     * 创建空响应消息
     * 
     * @return 统一的空响应消息
     */
    Message createEmptyResponse();
    
    /**
     * 验证地址格式
     * 
     * @param address 要验证的地址
     * @return 地址是否有效
     */
    boolean validateAddress(EndpointAddress address);
    
    /**
     * 获取协议特定的配置
     * 
     * @return 配置映射
     */
    Map<String, Object> getProtocolConfig();
    
    // ========== 向后兼容方法（已废弃） ==========
    
    /**
     * @deprecated 使用 {@link #adaptInbound(Object, Object)} 替代
     */
    @Deprecated
    default Message adaptInbound(Object protocolSpecificData, Connection connection) {
        throw new UnsupportedOperationException("Legacy method not supported in generic adapter");
    }
    
    /**
     * @deprecated 使用 {@link #adaptOutbound(Message, Object)} 替代
     */
    @Deprecated
    default Object adaptOutbound(Message message, Connection connection) {
        throw new UnsupportedOperationException("Legacy method not supported in generic adapter");
    }
    
    /**
     * @deprecated 使用 {@link #createClientConnection(EndpointAddress, Map)} 替代
     */
    @Deprecated
    default Connection createConnection(EndpointAddress address, Map<String, Object> options) {
        return createClientConnection(address, options);
    }
} 