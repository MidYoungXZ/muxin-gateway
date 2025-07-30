package com.muxin.gateway.core.plus.message;

import com.muxin.gateway.core.plus.common.AttributesHolder;

/**
 * @author: yangxz
 * @description:
 */
public interface ServerExchange<Req extends Message, Res extends Message> extends AttributesHolder {

    Protocol protocol();

    Req request();

    Req setRequest(Req request);

    Res response();

    Res setResponse(Res response);
}
