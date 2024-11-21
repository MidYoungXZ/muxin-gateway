package com.muxin.gateway.core.utils;

import com.muxin.gateway.core.common.ResponseStatusEnum;
import com.muxin.gateway.core.http.DefaultHttpServerResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.util.Map;

public class ResponseUtil {


    /**
     * 根据枚举和消息体生成 FullHttpResponse
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @param body       响应体内容
     * @return FullHttpResponse 对象
     */
    public static FullHttpResponse createResponse(ResponseStatusEnum statusEnum, String body) {
        return createResponse(statusEnum, body, null);
    }

    /**
     * 根据枚举、消息体和自定义头生成 FullHttpResponse
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @param body       响应体内容
     * @param headers    自定义 HTTP 头
     * @return FullHttpResponse 对象
     */
    public static FullHttpResponse createResponse(ResponseStatusEnum statusEnum, String body, Map<String, String> headers) {
        // 如果 body 为 null，设置为空字符串
        String responseBody = body == null ? "" : body;

        // 创建响应对象

        DefaultHttpServerResponse defaultHttpServerResponse = new DefaultHttpServerResponse();


        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                statusEnum.httpStatus(),
                Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8)
        );

        // 设置默认的 Content-Type 和 Content-Length
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 设置自定义的头部信息
        if (headers != null) {
            headers.forEach(response.headers()::set);
        }
        return response;
    }

    /**
     * 快速生成仅包含状态码的 FullHttpResponse
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @return FullHttpResponse 对象
     */
    public static FullHttpResponse createEmptyResponse(ResponseStatusEnum statusEnum) {
        return createResponse(statusEnum, null);
    }

    /**
     * 错误响应生成器
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @param errorMsg   错误描述
     * @return FullHttpResponse 对象
     */
    public static FullHttpResponse createErrorResponse(ResponseStatusEnum statusEnum, String errorMsg) {
        String body = String.format("{\"error_code\":\"%s\", \"message\":\"%s\"}",
                statusEnum.internalStatus(), errorMsg);
        return createResponse(statusEnum, body);
    }
}
