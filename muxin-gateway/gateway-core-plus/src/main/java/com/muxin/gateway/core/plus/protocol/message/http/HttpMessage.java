package com.muxin.gateway.core.plus.protocol.message.http;

import com.muxin.gateway.core.plus.protocol.message.*;

import java.net.URL;
import java.util.UUID;

/**
 * HTTP消息实现
 *
 * @author muxin
 */
public class HttpMessage implements Message {

    private final String messageId;
    private final MessageType type;
    private final Protocol protocol;
    private final URL url;
    private final String method;
    private final MessageHeaders headers;
    private final MessageBody body;
    private final MessageMetadata metadata;

    public HttpMessage(String messageId, MessageType type, Protocol protocol,
                       URL url, String method, MessageHeaders headers,
                       MessageBody body, MessageMetadata metadata) {
        this.messageId = messageId;
        this.type = type;
        this.protocol = protocol;
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.metadata = metadata;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public MessageHeaders getHeaders() {
        return headers;
    }

    @Override
    public MessageBody getBody() {
        return body;
    }

    @Override
    public MessageMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Message createResponse() {
        // 创建响应消息
        return new HttpMessage(
            UUID.randomUUID().toString(),  // 新的消息ID
            MessageType.RESPONSE,          // 响应类型
            protocol,                      // 相同协议
            url,                          // 相同URL
            method,                       // 相同方法
            new HttpHeaders(),            // 空的响应头
            HttpBody.empty(),             // 空的响应体
            metadata                      // 相同元数据
        );
    }

    @Override
    public Message copy() {
        // 创建消息副本
        return new HttpMessage(
            messageId,
            type,
            protocol,
            url,
            method,
            headers,
            body,
            metadata
        );
    }

    // Builder模式
    public static class Builder {
        private String messageId = UUID.randomUUID().toString();
        private MessageType type = MessageType.REQUEST;
        private Protocol protocol;
        private URL url;
        private String method = "GET";
        private MessageHeaders headers = new HttpHeaders();
        private MessageBody body = HttpBody.empty();
        private MessageMetadata metadata = HttpMetadata.builder().build();

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder url(URL url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder headers(MessageHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(MessageBody body) {
            this.body = body;
            return this;
        }

        public Builder metadata(MessageMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public HttpMessage build() {
            return new HttpMessage(messageId, type, protocol, url, method, headers, body, metadata);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // 静态工厂方法
    public static HttpMessage request(Protocol protocol, URL url, String method) {
        return builder()
                .type(MessageType.REQUEST)
                .protocol(protocol)
                .url(url)
                .method(method)
                .build();
    }

    public static HttpMessage response(Protocol protocol, URL url, String method) {
        return builder()
                .type(MessageType.RESPONSE)
                .protocol(protocol)
                .url(url)
                .method(method)
                .build();
    }
} 