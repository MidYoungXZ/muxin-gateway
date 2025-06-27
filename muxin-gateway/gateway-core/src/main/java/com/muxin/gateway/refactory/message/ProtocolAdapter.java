package com.muxin.gateway.refactory.message;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.Map;

/**
 * 泛型协议适配器接口
 * 支持类型安全的协议转换，提供完整的协议无关性
 *
 * @author muxin
 */
public interface ProtocolAdapter {

    /**
     * 支持的协议
     */
    Protocol getSupportedProtocol();


    /**
     * 是否支持该协议
     *
     * @param request 协议特定的请求对象
     * @return 是否支持
     */
    boolean support(Object request);


    /**
     * 入站适配：协议特定请求 -> 统一消息
     *
     * @param request 协议特定的请求对象
     * @param context 协议特定的上下文对象
     * @return 统一的消息对象
     */
    Message adaptInbound(Object request, UniversalRequestContext context);

    /**
     * 出站适配：统一消息 -> 协议特定响应
     *
     * @param message 统一的消息对象
     * @param context 协议特定的上下文对象
     * @return 协议特定的响应对象
     */
    Object adaptOutbound(Message message, UniversalRequestContext context);

    /**
     * 协议转换：统一消息 -> 目标协议消息
     *
     * @param sourceMessage 源消息对象
     * @param targetProtocol 目标协议
     * @return 目标协议消息对象
     */
    Message convertProtocol(Message sourceMessage, Protocol targetProtocol);


    /**
     * 创建服务器端连接
     *
     * @param context 协议特定的上下文对象
     * @return 协议特定的连接对象
     */
    Connection createServerConnection(UniversalRequestContext context);

    /**
     * 创建客户端连接
     *
     * @param address 目标地址
     * @param options 连接选项
     * @return 协议特定的连接对象
     */
    Connection createClientConnection(EndpointAddress address, Map<String, Object> options);

    /**
     * 获取协议特定的配置
     *
     * @return 配置映射
     */
    Map<String, Object> getProtocolConfig();


} 