package com.muxin.gateway.core.plus.msg;

/**
 * @projectname: muxin-gateway
 * @filename: ServerExchange
 * @author: yangxz
 * @data:2025/7/16 22:06
 * @description:
 */
public interface ServerExchange<Req extends Message, Res extends Message> extends AttributesHolder {

    Protocol protocol();

    Req request();

    Req setRequest(Req request);

    Res response();

    Res setResponse(Res response);
}
