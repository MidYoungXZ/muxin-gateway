package com.muxin.gateway.core.plus.message.http;

import com.muxin.gateway.core.plus.message.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;

/**
 * HTTP消息接口
 * 定义HTTP协议消息的基本接口，包含HTTP版本和请求头
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpMessage extends Message {

    HttpVersion protocolVersion();

    void setProtocolVersion(HttpVersion httpVersion);

    HttpHeaders headers();

    void header(CharSequence name, CharSequence value);
}
