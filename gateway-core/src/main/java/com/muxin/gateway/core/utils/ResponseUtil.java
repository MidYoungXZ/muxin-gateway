package com.muxin.gateway.core.utils;

import com.muxin.gateway.core.common.ResponseStatusEnum;
import com.muxin.gateway.core.http.DefaultHttpServerResponse;
import com.muxin.gateway.core.http.HttpServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.asynchttpclient.Response;

import java.util.Map;

public class ResponseUtil {


    /**
     * 根据枚举和消息体生成 FullHttpResponse
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @param body       响应体内容
     * @return FullHttpResponse 对象
     */
    public static HttpServerResponse createResponse(ResponseStatusEnum statusEnum, String body) {
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
    public static HttpServerResponse createResponse(ResponseStatusEnum statusEnum, String body, Map<String, String> headers) {
        // 如果 body 为 null，设置为空字符串
        String responseBody = body == null ? "" : body;
        if (headers != null) {
            // 设置默认的 Content-Type 和 Content-Length
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString());
        }
        // 创建响应对象
        return new DefaultHttpServerResponse(statusEnum.httpStatus(), headers, responseBody);
    }

    /**
     * 快速生成仅包含状态码的 FullHttpResponse
     *
     * @param statusEnum ResponseStatusEnum 枚举值
     * @return FullHttpResponse 对象
     */
    public static HttpServerResponse createEmptyResponse(ResponseStatusEnum statusEnum) {
        return createResponse(statusEnum, null);
    }


    /**
     * 快速生成仅包含状态码的 FullHttpResponse
     *
     * @param clientResponse Response 代理客户端的响应对象
     * @return FullHttpResponse 对象
     */
    public static HttpServerResponse clientResponseToHttpServerResponse(Response clientResponse) {
        HttpHeaders responseHeaders = clientResponse.getHeaders();
        HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(clientResponse.getStatusCode());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(clientResponse.getResponseBodyAsBytes());
        return new DefaultHttpServerResponse(HttpVersion.HTTP_1_1, responseStatus, responseHeaders, byteBuf, System.currentTimeMillis());
    }


}
