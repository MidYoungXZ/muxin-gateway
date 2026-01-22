package com.muxin.gateway.core.plus.message;

import com.muxin.gateway.core.plus.common.AttributesHolder;

/**
 * 消息接口
 * 定义网关中所有消息的基本接口，支持多协议消息处理
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Message extends AttributesHolder {

    MessageType messageType();

    Protocol protocol();

}
