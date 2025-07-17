package com.muxin.gateway.core.plus.message;

import com.muxin.gateway.core.plus.common.AttributesHolder;

/**
 * @projectname: muxin-gateway
 * @filename: Msg
 * @author: yangxz
 * @data:2025/7/16 21:07
 * @description:
 */
public interface Message extends AttributesHolder {

    MessageType messageType();

    Protocol protocol();

}
