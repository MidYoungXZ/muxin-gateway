package com.muxin.gateway.core.utils;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.common.ResponseStatusEnum;
import com.muxin.gateway.core.common.exception.GatewayException;
import com.muxin.gateway.core.http.HttpServerOperations;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Objects;

import static com.muxin.gateway.core.common.GatewayConstants.INTERNAL_STATUS;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/21 11:38
 */
public class HttpServerOperationsUtil {




    public static void createResponse(HttpServerOperations operations, ResponseStatusEnum statusEnum, String content) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(content.getBytes());
        createResponse(operations, statusEnum, byteBuf, null);
    }

    public static void createResponse(HttpServerOperations operations, ResponseStatusEnum statusEnum) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(GatewayConstants.BLANK_SEPARATOR.getBytes());
        createResponse(operations, statusEnum, byteBuf, null);
    }

    /**
     * @param operations 网关内部请求响应操作类
     * @param statusEnum 网关内部状态枚举
     * @param content    http报文体
     * @param headers    http报文头Map
     */
    public static void createResponse(HttpServerOperations operations, ResponseStatusEnum statusEnum, ByteBuf content, Map<String, String> headers) {
        createResponse(operations, HttpVersion.HTTP_1_1, statusEnum.httpStatus(), statusEnum.internalStatus(), content, null);
        if (!ObjectUtils.isEmpty(headers)) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                operations.getResponse().headers().add(headerEntry.getKey(), headerEntry.getValue());
            }
        }
    }


    /**
     * @param operations         网关内部请求响应操作类
     * @param httpVersion        http版本
     * @param httpResponseStatus http状态码
     * @param internalStatus     网关内置状态码
     * @param content            http报文体
     * @param headers            http报文头
     * @return
     */
    public static void createResponse(HttpServerOperations operations,
                                      HttpVersion httpVersion,
                                      HttpResponseStatus httpResponseStatus,
                                      String internalStatus,
                                      ByteBuf content,
                                      HttpHeaders headers) {
        if (Objects.isNull(operations)) {
            throw new GatewayException("operations must not null");
        }
        DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                httpVersion, httpResponseStatus, content);
        if (!ObjectUtils.isEmpty(headers)) {
            fullHttpResponse.headers().add(headers);
        }
        if (ObjectUtils.isEmpty(internalStatus)) {
            fullHttpResponse.headers().add(INTERNAL_STATUS, internalStatus);
        }
        operations.setResponse(fullHttpResponse);
    }

}