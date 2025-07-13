package com.muxin.gateway.core.plus.protocol.message;

import com.muxin.gateway.core.plus.route.RequestContext;

/**
 * @author: yangxz
 * @description:
 */
public interface MessageCodec {


    /**
     * 检查是否支持指定的协议转换
     *
     * @param sourceProtocol 源协议
     * @return 是否支持转换
     */
    boolean supports(Protocol sourceProtocol);

    /**
     * 将协议特定数据转换为统一消息格式
     *
     * @param protocolData 协议特定的数据对象
     * @param context      请求上下文
     * @return 统一消息对象
     */
    Message convertToMessage(ProtocolData protocolData, RequestContext context);

    /**
     * 将统一消息格式转换为协议特定数据
     *
     * @param message 统一消息对象
     * @param context 请求上下文
     * @return 协议特定的数据对象
     */
    ProtocolData convertFromMessage(Message message, RequestContext context);

}
