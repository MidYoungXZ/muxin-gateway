package com.muxin.gateway.core.plus.message.http;

import com.muxin.gateway.core.plus.message.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @projectname: muxin-gateway
 * @filename: HttpMessage
 * @author: yangxz
 * @data:2025/7/16 21:12
 * @description:
 */
public interface HttpMessage extends Message {

    HttpVersion protocolVersion();

    void setProtocolVersion(HttpVersion httpVersion);

    HttpHeaders headers();

    void header(CharSequence name, CharSequence value);
}
