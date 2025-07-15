package com.muxin.gateway.core.plus.protocol.message;

import com.muxin.gateway.core.plus.protocol.message.http.HttpBody;
import com.muxin.gateway.core.plus.protocol.message.http.HttpHeaders;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMessage;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMetadata;
import com.muxin.gateway.core.plus.route.RequestContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP 消息编解码器实现
 * 负责在 Netty HTTP 对象和统一 Message 对象之间进行转换
 *
 * @author muxin
 */
@Slf4j
public class HttpMessageCodec implements MessageCodec {

    // ========== 协议支持检查 ==========

    @Override
    public boolean supports(Protocol sourceProtocol) {
        if (sourceProtocol == null) {
            return false;
        }

        // 支持 HTTP 协议的所有版本
        return "HTTP".equalsIgnoreCase(sourceProtocol.type());
    }

    // ========== 编解码核心方法 ==========

    @Override
    public Message convertToMessage(ProtocolData protocolData, RequestContext context) {
        if (protocolData == null || protocolData.getData() == null) {
            throw new IllegalArgumentException("协议数据不能为空");
        }

        Object rawData = protocolData.getData();

        try {
            if (rawData instanceof FullHttpRequest request) {
                return convertHttpRequestToMessage(request, protocolData.getProtocol(), context);
            } else if (rawData instanceof FullHttpResponse response) {
                return convertHttpResponseToMessage(response, protocolData.getProtocol(), context);
            } else {
                throw new IllegalArgumentException("不支持的 HTTP 数据类型: " + rawData.getClass().getName());
            }
        } catch (Exception e) {
            log.error("[HttpMessageCodec] 转换为 Message 失败", e);
            throw new RuntimeException("HTTP 消息编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ProtocolData convertFromMessage(Message message, RequestContext context) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }

        if (!(message instanceof HttpMessage)) {
            throw new IllegalArgumentException("不支持的消息类型: " + message.getClass().getName());
        }

        HttpMessage httpMessage = (HttpMessage) message;

        try {
            Object httpObject;
            if (httpMessage.getType() == MessageType.REQUEST) {
                httpObject = convertMessageToHttpRequest(httpMessage);
            } else if (httpMessage.getType() == MessageType.RESPONSE) {
                httpObject = convertMessageToHttpResponse(httpMessage);
            } else {
                throw new IllegalArgumentException("不支持的消息类型: " + httpMessage.getType());
            }

            return new ProtocolData(httpMessage.getProtocol(), httpObject);

        } catch (Exception e) {
            log.error("[HttpMessageCodec] 转换为 ProtocolData 失败", e);
            throw new RuntimeException("HTTP 消息解码失败: " + e.getMessage(), e);
        }
    }

    // ========== HTTP 请求转换 ==========

    /**
     * 将 Netty FullHttpRequest 转换为统一 Message 对象
     */
    private Message convertHttpRequestToMessage(FullHttpRequest request, Protocol protocol, RequestContext context) throws Exception {
        // 1. 生成消息ID
        String messageId = UUID.randomUUID().toString();
        // 2. 构建URL
        URL url = new URL(request.uri());
        // 3. 转换头部
        MessageHeaders headers = convertNettyHeaders(request.headers());

        // 4. 转换消息体
        MessageBody body = convertNettyContent(request.content(), getContentType(request.headers()));

        // 5. 构建元数据
        MessageMetadata metadata = buildRequestMetadata(request, context);

        // 6. 创建 HttpMessage
        return new HttpMessage(
                messageId,
                MessageType.REQUEST,
                protocol,
                url,
                request.method().name(),
                headers,
                body,
                metadata
        );
    }

    /**
     * 将 Netty FullHttpResponse 转换为统一 Message 对象
     */
    private Message convertHttpResponseToMessage(FullHttpResponse response, Protocol protocol, RequestContext context) {
        // 1. 生成消息ID
        String messageId = UUID.randomUUID().toString();

        // 2. 构建URL（响应通常使用请求的URL）
        URL url = null;
        if (context.getInboundMessage() != null) {
            url = context.getInboundMessage().url();
        }

        // 3. 转换头部
        MessageHeaders headers = convertNettyHeaders(response.headers());

        // 4. 转换消息体
        MessageBody body = convertNettyContent(response.content(), getContentType(response.headers()));

        // 5. 构建元数据
        MessageMetadata metadata = buildResponseMetadata(response, context);

        // 6. 创建 HttpMessage
        return new HttpMessage(
                messageId,
                MessageType.RESPONSE,
                protocol,
                url,
                context.getInboundMessage().method(),
                headers,
                body,
                metadata
        );
    }

    // ========== HTTP 对象转换 ==========

    /**
     * 将统一 Message 对象转换为 Netty FullHttpRequest
     */
    private FullHttpRequest convertMessageToHttpRequest(HttpMessage message) {
        // 1. 构建 HTTP 方法
        HttpMethod method = HttpMethod.valueOf(message.method().toUpperCase());

        // 2. 构建请求体
        ByteBuf content = buildContentFromMessage(message.getBody());

        // 3. 创建 HTTP 请求
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, message.url().toString(), content
        );

        // 5. 设置头部
        setNettyHeaders(request.headers(), message.getHeaders());

        // 6. 设置默认头部
        setDefaultRequestHeaders(request, message);

        return request;
    }

    /**
     * 将统一 Message 对象转换为 Netty FullHttpResponse
     */
    private FullHttpResponse convertMessageToHttpResponse(HttpMessage message) {
        // 1. 构建响应状态（从元数据中获取，默认 200）
        HttpResponseStatus status = HttpResponseStatus.OK;
        if (message.getMetadata() != null) {
            Integer statusCode = message.getMetadata().getAttribute("statusCode", Integer.class);
            if (statusCode != null) {
                status = HttpResponseStatus.valueOf(statusCode);
            }
        }

        // 2. 构建响应体
        ByteBuf content = buildContentFromMessage(message.getBody());

        // 3. 创建 HTTP 响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, content
        );

        // 4. 设置头部
        setNettyHeaders(response.headers(), message.getHeaders());

        // 5. 设置默认头部
        setDefaultResponseHeaders(response, message);

        return response;
    }

    // ========== 辅助转换方法 ==========

    /**
     * 转换 Netty HTTP 头部为统一 MessageHeaders
     */
    private MessageHeaders convertNettyHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        HttpHeaders headers = new HttpHeaders();

        for (Map.Entry<String, String> entry : nettyHeaders) {
            headers.set(entry.getKey(), entry.getValue());
        }

        return headers;
    }

    /**
     * 转换 Netty ByteBuf 内容为统一 MessageBody
     */
    private MessageBody convertNettyContent(ByteBuf content, String contentType) {
        if (content == null || content.readableBytes() == 0) {
            return HttpBody.empty();
        }

        // 读取字节数据
        byte[] bytes = new byte[content.readableBytes()];
        content.getBytes(content.readerIndex(), bytes);

        return new HttpBody(bytes, contentType != null ? contentType : "application/octet-stream");
    }


    /**
     * 从 MessageBody 构建 ByteBuf 内容
     */
    private ByteBuf buildContentFromMessage(MessageBody body) {
        if (body == null || body.isEmpty()) {
            return Unpooled.EMPTY_BUFFER;
        }

        return Unpooled.wrappedBuffer(body.getBytes());
    }

    /**
     * 设置 Netty HTTP 头部
     */
    private void setNettyHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders, MessageHeaders messageHeaders) {
        if (messageHeaders == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : messageHeaders.asMap().entrySet()) {
            if (entry.getValue() != null) {
                nettyHeaders.set(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    /**
     * 设置默认请求头部
     */
    private void setDefaultRequestHeaders(FullHttpRequest request, HttpMessage message) {
        io.netty.handler.codec.http.HttpHeaders headers = request.headers();

        // 设置 Content-Length
        if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            headers.set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        }

        // 设置 Host
        if (!headers.contains(HttpHeaderNames.HOST) && message.url() != null) {
            String host = message.url().getHost();
            int port = message.url().getPort();
            if (port != -1 && port != 80 && port != 443) {
                host += ":" + port;
            }
            headers.set(HttpHeaderNames.HOST, host);
        }

        // 设置 User-Agent
        if (!headers.contains(HttpHeaderNames.USER_AGENT)) {
            headers.set(HttpHeaderNames.USER_AGENT, "MuxinGateway/1.0");
        }
    }

    /**
     * 设置默认响应头部
     */
    private void setDefaultResponseHeaders(FullHttpResponse response, HttpMessage message) {
        io.netty.handler.codec.http.HttpHeaders headers = response.headers();

        // 设置 Content-Length
        if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }

        // 设置 Server
        if (!headers.contains(HttpHeaderNames.SERVER)) {
            headers.set(HttpHeaderNames.SERVER, "MuxinGateway/1.0");
        }

        // 设置 Date
        if (!headers.contains(HttpHeaderNames.DATE)) {
            headers.set(HttpHeaderNames.DATE, new Date().toString());
        }
    }

    /**
     * 获取 Content-Type
     */
    private String getContentType(io.netty.handler.codec.http.HttpHeaders headers) {
        return headers.get(HttpHeaderNames.CONTENT_TYPE);
    }

    // ========== 元数据构建 ==========

    /**
     * 构建请求元数据
     */
    private MessageMetadata buildRequestMetadata(FullHttpRequest request, RequestContext context) {
        HttpMetadata.Builder builder = HttpMetadata.builder()
                .timestamp(System.currentTimeMillis())
                .receiveTime(System.currentTimeMillis())
                .traceId(UUID.randomUUID().toString())
                .spanId(UUID.randomUUID().toString());

        // 从上下文获取连接信息
        if (context != null && context.serverConnection() != null) {
            builder.connectionId(context.serverConnection().getConnectionId());
        }
        return builder.build();
    }

    /**
     * 构建响应元数据
     */
    private MessageMetadata buildResponseMetadata(FullHttpResponse response, RequestContext context) {
        HttpMetadata.Builder builder = HttpMetadata.builder()
                .timestamp(System.currentTimeMillis())
                .sendTime(System.currentTimeMillis());

        // 从请求元数据继承信息
        if (context != null && context.getInboundMessage() != null
                && context.getInboundMessage().getMetadata() != null) {
            MessageMetadata requestMeta = context.getInboundMessage().getMetadata();
            builder.traceId(requestMeta.getTraceId())
                    .connectionId(requestMeta.getConnectionId())
                    .routeId(requestMeta.getRouteId())
                    .serviceName(requestMeta.getServiceName());
        }
        // 添加 HTTP 响应特定属性
        builder.attribute("statusCode", response.status().code());
        builder.attribute("statusText", response.status().reasonPhrase());
        return builder.build();
    }

    // ========== 工具方法 ==========

    @Override
    public String toString() {
        return "HttpMessageCodec{supportedProtocol=HTTP}";
    }
} 