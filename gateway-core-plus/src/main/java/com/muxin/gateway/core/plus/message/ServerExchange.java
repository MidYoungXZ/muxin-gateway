package com.muxin.gateway.core.plus.message;

import com.muxin.gateway.core.plus.common.AttributesHolder;

/**
 * 服务器交换接口
 * 定义服务器端请求和响应的交换上下文，用于在网关处理过程中传递数据
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ServerExchange<Req extends Message, Res extends Message> extends AttributesHolder {

    Protocol protocol();

    Req request();

    Req setRequest(Req request);

    Res response();

    Res setResponse(Res response);
}
