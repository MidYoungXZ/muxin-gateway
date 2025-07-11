package com.muxin.gateway.core.plus.protocol.message;

import com.muxin.gateway.core.plus.protocol.message.http.HttpBody;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMessage;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMetadata;
import com.muxin.gateway.core.plus.protocol.message.http.HttpHeaders;
import com.muxin.gateway.core.plus.route.RequestContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * HTTP协议转换器实现
 * 负责HTTP协议与Universal协议之间的双向转换
 *
 * @author muxin
 * @since 2.0
 */
@Slf4j
public class HttpProtocolConverter implements ProtocolConverter {

    private static final Protocol HTTP_PROTOCOL = new Protocol.HttpProtocol();
    private static final Protocol UNIVERSAL_PROTOCOL = Protocol.UNIVERSAL;

    @Override
    public Protocol getSupportedSourceProtocol() {
        return HTTP_PROTOCOL;
    }

    @Override
    public Protocol getSupportedTargetProtocol() {
        return UNIVERSAL_PROTOCOL;
    }

    @Override
    public Message convertToMessage(Object protocolSpecific, RequestContext context) throws ProtocolConversionException {

        if (!(protocolSpecific instanceof FullHttpRequest)) {
            throw new ProtocolConversionException("协议特定对象必须是FullHttpRequest类型", HTTP_PROTOCOL, UNIVERSAL_PROTOCOL);
        }

        FullHttpRequest httpRequest = (FullHttpRequest) protocolSpecific;

        try {
            // 创建消息组件
            String messageId = UUID.randomUUID().toString();
            MessageType messageType = MessageType.REQUEST;

            // 创建HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            for (Map.Entry<String, String> entry : httpRequest.headers()) {
                headers.set(entry.getKey(), entry.getValue());
            }

            // 创建HTTP Body
            byte[] bodyBytes = extractBody(httpRequest);
            HttpBody body = new HttpBody(bodyBytes);

            // 创建HTTP Metadata
            HttpMetadata metadata = new HttpMetadata();
            metadata.setMethod(httpRequest.method().name());
            metadata.setAttribute("uri", httpRequest.uri());
            metadata.setPath(extractPath(httpRequest));
            metadata.setAttribute("version", httpRequest.protocolVersion().text());

            // 创建Universal消息
            Message universalMessage = new HttpMessage(
                    messageId,
                    messageType,
                    UNIVERSAL_PROTOCOL,
                    headers,
                    body,
                    metadata
            );

            log.debug("[HttpProtocolConverter] HTTP请求转换为Universal消息完成 - URI: {}", httpRequest.uri());
            return universalMessage;

        } catch (Exception e) {
            log.error("[HttpProtocolConverter] HTTP到Universal转换失败", e);
            throw new ProtocolConversionException("HTTP到Universal转换失败", e, HTTP_PROTOCOL, UNIVERSAL_PROTOCOL);
        }
    }

    @Override
    public Object convertFromMessage(Message universal, RequestContext context)
            throws ProtocolConversionException {

        if (universal == null) {
            throw new ProtocolConversionException("Universal消息不能为空", UNIVERSAL_PROTOCOL, HTTP_PROTOCOL);
        }

        try {
            // 根据消息类型创建不同的HTTP对象
            if (universal.getType() == MessageType.REQUEST) {
                return convertToHttpRequest(universal);
            } else if (universal.getType() == MessageType.RESPONSE) {
                return convertToHttpResponse(universal);
            } else {
                throw new ProtocolConversionException("不支持的消息类型: " + universal.getType(),
                        UNIVERSAL_PROTOCOL, HTTP_PROTOCOL);
            }

        } catch (Exception e) {
            log.error("[HttpProtocolConverter] Universal到HTTP转换失败", e);
            throw new ProtocolConversionException("Universal到HTTP转换失败", e, UNIVERSAL_PROTOCOL, HTTP_PROTOCOL);
        }
    }

    @Override
    public boolean supports(Protocol sourceProtocol, Protocol targetProtocol) {
        return (HTTP_PROTOCOL.equals(sourceProtocol) && UNIVERSAL_PROTOCOL.equals(targetProtocol)) ||
                (UNIVERSAL_PROTOCOL.equals(sourceProtocol) && HTTP_PROTOCOL.equals(targetProtocol));
    }

    @Override
    public ConversionMetrics getMetrics() {
        return new SimpleConversionMetrics();
    }

    /**
     * 提取HTTP请求的路径
     */
    private String extractPath(FullHttpRequest httpRequest) {
        String uri = httpRequest.uri();
        int queryIndex = uri.indexOf('?');
        return queryIndex > 0 ? uri.substring(0, queryIndex) : uri;
    }

    /**
     * 提取HTTP请求体
     */
    private byte[] extractBody(FullHttpRequest httpRequest) {
        ByteBuf content = httpRequest.content();
        if (!content.isReadable()) {
            return new byte[0];
        }

        byte[] bodyBytes = new byte[content.readableBytes()];
        content.getBytes(content.readerIndex(), bodyBytes);
        return bodyBytes;
    }

    /**
     * 将Universal消息转换为HTTP请求
     */
    private FullHttpRequest convertToHttpRequest(Message universal) {
        // 从元数据获取HTTP信息
        HttpMetadata httpMetadata = (HttpMetadata) universal.getMetadata();

        // 构建HTTP方法
        String methodName = httpMetadata != null ? httpMetadata.getMethod() : "GET";
        HttpMethod httpMethod = HttpMethod.valueOf(methodName);

        // 构建URI
        String uri = httpMetadata != null ? httpMetadata.getAttribute("uri", String.class) : "/";

        // 创建HTTP请求
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                httpMethod,
                uri
        );

        // 设置Header
        MessageHeaders headers = universal.getHeaders();
        if (headers != null) {
            Map<String, Object> headerMap = headers.asMap();
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                httpRequest.headers().set(entry.getKey(), entry.getValue().toString());
            }
        }

        // 设置Body
        MessageBody messageBody = universal.getBody();
        if (messageBody != null && !messageBody.isEmpty()) {
            byte[] bodyBytes = messageBody.getBytes();
            httpRequest.content().writeBytes(bodyBytes);
            httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, bodyBytes.length);
        }

        return httpRequest;
    }

    /**
     * 将Universal消息转换为HTTP响应
     */
    private FullHttpResponse convertToHttpResponse(Message universal) {
        // 确定HTTP状态码
        HttpResponseStatus status = determineHttpStatus(universal);

        // 创建HTTP响应
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status
        );

        // 设置Header
        MessageHeaders headers = universal.getHeaders();
        if (headers != null) {
            Map<String, Object> headerMap = headers.asMap();
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                httpResponse.headers().set(entry.getKey(), entry.getValue().toString());
            }
        }

        // 设置Body
        MessageBody messageBody = universal.getBody();
        if (messageBody != null && !messageBody.isEmpty()) {
            byte[] bodyBytes = messageBody.getBytes();
            httpResponse.content().writeBytes(bodyBytes);
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bodyBytes.length);
        }

        // 设置默认Content-Type（如果没有指定）
        if (!httpResponse.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        }

        return httpResponse;
    }

    /**
     * 根据Universal消息确定HTTP状态码
     */
    private HttpResponseStatus determineHttpStatus(Message universal) {
        // 从元数据中获取状态码
        MessageMetadata metadata = universal.getMetadata();
        if (metadata != null) {
            Object statusCode = metadata.getAttribute("statusCode", Object.class);
            if (statusCode instanceof Integer) {
                return HttpResponseStatus.valueOf((Integer) statusCode);
            }
            if (statusCode instanceof String) {
                try {
                    return HttpResponseStatus.valueOf(Integer.parseInt((String) statusCode));
                } catch (NumberFormatException e) {
                    log.warn("[HttpProtocolConverter] 无效的状态码: {}", statusCode);
                }
            }
        }

        // 默认状态码
        return HttpResponseStatus.OK;
    }

    /**
     * 简单的转换指标实现
     */
    private static class SimpleConversionMetrics implements ConversionMetrics {

        @Override
        public long getTotalConversions() {
            return 0; // 简化实现
        }

        @Override
        public long getSuccessfulConversions() {
            return 0; // 简化实现
        }

        @Override
        public long getFailedConversions() {
            return 0; // 简化实现
        }

        @Override
        public double getAverageConversionTime() {
            return 0.0; // 简化实现
        }
    }
} 