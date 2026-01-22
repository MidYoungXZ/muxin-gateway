package com.muxin.gateway.core.plus.message.converter;

import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;

/**
 * 协议转换器接口
 * 负责在不同协议版本或协议类型之间进行消息转换
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ProtocolConverter {

    /**
     * 判断是否支持指定的协议转换
     *
     * @param fromProtocol 源协议
     * @param toProtocol   目标协议
     * @return true表示支持，false表示不支持
     */
    boolean supports(Protocol fromProtocol, Protocol toProtocol);

    /**
     * 转换请求消息
     *
     * @param request     原始请求消息
     * @param fromProtocol 源协议
     * @param toProtocol   目标协议
     * @return 转换后的请求消息
     * @throws ProtocolConversionException 转换失败时抛出
     */
    Message convertRequest(Message request, Protocol fromProtocol, Protocol toProtocol)
            throws ProtocolConversionException;

    /**
     * 转换响应消息
     *
     * @param response    原始响应消息
     * @param fromProtocol 源协议
     * @param toProtocol   目标协议
     * @return 转换后的响应消息
     * @throws ProtocolConversionException 转换失败时抛出
     */
    Message convertResponse(Message response, Protocol fromProtocol, Protocol toProtocol)
            throws ProtocolConversionException;

    /**
     * 获取转换器名称
     */
    String getName();

    /**
     * 获取转换器描述
     */
    String getDescription();
}
