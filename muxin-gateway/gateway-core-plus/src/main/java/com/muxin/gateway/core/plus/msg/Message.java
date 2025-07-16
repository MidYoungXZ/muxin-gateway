package com.muxin.gateway.core.plus.msg;

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
