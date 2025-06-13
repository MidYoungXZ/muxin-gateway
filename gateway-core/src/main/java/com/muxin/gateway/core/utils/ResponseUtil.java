package com.muxin.gateway.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;


@Slf4j
public class ResponseUtil {


    public static FullHttpResponse error() {
        return error(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
    }

    public static FullHttpResponse error(String message) {
        return createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static FullHttpResponse createResponse(HttpResponseStatus statusEnum, String body) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusEnum, Unpooled.wrappedBuffer(body.getBytes()));
    }

    public static FullHttpResponse createResponse(HttpResponseStatus statusEnum) {
        return createResponse(statusEnum, statusEnum.reasonPhrase());
    }

    public static FullHttpResponse convertToFullHttpResponse(Response response) {
        if (response == null) {
            return null;
        }
        try {
            // 创建HTTP版本
            HttpVersion httpVersion = HttpVersion.HTTP_1_1;
            // 创建响应状态
            HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatusCode());
            // 获取响应体
            ByteBuf content = Unpooled.EMPTY_BUFFER;
            if (response.hasResponseBody()) {
                content = Unpooled.wrappedBuffer(response.getResponseBodyAsByteBuffer());
            }
            // 创建FullHttpResponsew
            FullHttpResponse fullResponse = new DefaultFullHttpResponse(httpVersion, status, content);
            // 复制响应头
            fullResponse.headers().add(response.getHeaders());
            // 设置Content-Length（如果原响应没有设置的话）
            if (!fullResponse.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
                fullResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            }
            return fullResponse;
        } catch (Exception e) {
            log.error("Failed to convert Response to FullHttpResponse: " + e.getMessage(), e);
            return createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
