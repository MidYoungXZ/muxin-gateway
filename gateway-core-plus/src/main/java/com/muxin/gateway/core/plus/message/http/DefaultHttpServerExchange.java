package com.muxin.gateway.core.plus.message.http;

import com.muxin.gateway.core.plus.message.MessageType;
import com.muxin.gateway.core.plus.message.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP服务器交换对象的默认实现
 * 将Netty的HTTP对象适配为网关的消息接口
 *
 * @author muxin
 */
@Slf4j
public class DefaultHttpServerExchange implements HttpServerExchange {

    // ========== 核心字段 ==========
    private final Protocol protocol;
    private final Map<String, Object> attributes;

    private volatile NettyHttpRequestAdapter httpRequestAdapter;
    private volatile NettyHttpResponseAdapter httpResponseAdapter;

    // ========== 构造函数 ==========

    /**
     * 构造函数
     *
     * @param request  Netty HTTP请求对象
     * @param protocol 协议信息
     */
    public DefaultHttpServerExchange(FullHttpRequest request, Protocol protocol) {
        Objects.requireNonNull(request, "HTTP请求不能为空");
        Objects.requireNonNull(protocol, "协议信息不能为空");
        this.protocol = protocol;
        this.attributes = new HashMap<>();
        this.httpRequestAdapter = new NettyHttpRequestAdapter(request, new HashMap<>());
        log.debug("创建HTTP服务器交换对象: {} {}", request.method(), request.uri());
    }

    // ========== ServerExchange 接口实现 ==========

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public HttpRequestMessage request() {
        return httpRequestAdapter;
    }

    @Override
    public HttpRequestMessage setRequest(HttpRequestMessage request) {
        // HTTP请求在服务器端是只读的，不支持修改
        throw new UnsupportedOperationException("HTTP服务器端不支持修改请求");
    }

    @Override
    public HttpResponseMessage response() {
        return httpResponseAdapter;
    }

    @Override
    public HttpResponseMessage setResponse(HttpResponseMessage response) {
        if (response instanceof NettyHttpResponseAdapter httpResponseAdapter) {
            // 如果是我们的适配器，直接使用底层对象
            this.httpResponseAdapter = httpResponseAdapter;
        } else if (response instanceof FullHttpResponse httpResponse) {
            this.httpResponseAdapter = new NettyHttpResponseAdapter(httpResponse, new HashMap<>());
        } else {
            throw new IllegalArgumentException("unknown response type: " + response.getClass());
        }
        return response;
    }

    // ========== AttributesHolder 接口实现 ==========

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // ========== 内部方法 ==========


    // ========== Netty HTTP请求适配器 ==========

    /**
     * 将Netty的FullHttpRequest适配为HttpRequestMessage接口
     */
    private static class NettyHttpRequestAdapter implements HttpRequestMessage {

        private final FullHttpRequest nettyRequest;
        private final Map<String, Object> attributes;
        private final String requestId;
        private final ZonedDateTime timestamp;

        public NettyHttpRequestAdapter(FullHttpRequest nettyRequest, Map<String, Object> attributes) {
            this.nettyRequest = nettyRequest;
            this.attributes = attributes;
            this.requestId = generateRequestId();
            this.timestamp = ZonedDateTime.now();
        }

        @Override
        public HttpMethod method() {
            return nettyRequest.method();
        }

        @Override
        public void setMethod(HttpMethod httpMethod) {
            nettyRequest.setMethod(httpMethod);
        }

        @Override
        public String uri() {
            return nettyRequest.uri();
        }

        @Override
        public String fullPath() {
            QueryStringDecoder decoder = new QueryStringDecoder(nettyRequest.uri());
            return decoder.path();
        }

        @Override
        public String requestId() {
            return requestId;
        }

        @Override
        public boolean isKeepAlive() {
            return HttpUtil.isKeepAlive(nettyRequest);
        }

        @Override
        public String param(CharSequence key) {
            QueryStringDecoder decoder = new QueryStringDecoder(nettyRequest.uri());
            return decoder.parameters().getOrDefault(key.toString(), java.util.Collections.emptyList())
                    .stream().findFirst().orElse(null);
        }

        @Override
        public Map<String, String> params() {
            QueryStringDecoder decoder = new QueryStringDecoder(nettyRequest.uri());
            Map<String, String> params = new java.util.HashMap<>();
            decoder.parameters().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    params.put(key, values.get(0));
                }
            });
            return params;
        }

        @Override
        public ZonedDateTime timestamp() {
            return timestamp;
        }

        @Override
        public HttpVersion protocolVersion() {
            return nettyRequest.protocolVersion();
        }

        @Override
        public void setProtocolVersion(HttpVersion httpVersion) {
            nettyRequest.setProtocolVersion(httpVersion);
        }

        @Override
        public HttpHeaders headers() {
            return nettyRequest.headers();
        }

        @Override
        public void header(CharSequence name, CharSequence value) {
            nettyRequest.headers().set(name, value);
        }

        @Override
        public MessageType messageType() {
            return MessageType.REQUEST;
        }

        @Override
        public Protocol protocol() {
            // 根据HTTP版本创建协议对象
            return createHttpProtocol(nettyRequest.protocolVersion());
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        /**
         * 生成请求ID
         */
        private String generateRequestId() {
            return System.currentTimeMillis() + "-" + System.nanoTime() % 10000;
        }

        /**
         * 获取请求Body内容
         */
        public String getBody() {
            if (nettyRequest.content().isReadable()) {
                return nettyRequest.content().toString(StandardCharsets.UTF_8);
            }
            return "";
        }

        /**
         * 获取Content-Type
         */
        public String getContentType() {
            return nettyRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        }
    }

    // ========== Netty HTTP响应适配器 ==========

    /**
     * 将Netty的FullHttpResponse适配为HttpResponseMessage接口
     */
    private static class NettyHttpResponseAdapter implements HttpResponseMessage {

        private final FullHttpResponse nettyResponse;
        private final Map<String, Object> attributes;

        public NettyHttpResponseAdapter(FullHttpResponse nettyResponse, Map<String, Object> attributes) {
            this.nettyResponse = nettyResponse;
            this.attributes = attributes;
        }

        @Override
        public HttpResponseStatus status() {
            return nettyResponse.status();
        }

        @Override
        public void setStatus(HttpResponseStatus httpResponseStatus) {
            nettyResponse.setStatus(httpResponseStatus);
        }

        @Override
        public HttpResponseMessage keepAlive(boolean keepAlive) {
            if (keepAlive) {
                nettyResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                nettyResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }
            return this;
        }

        @Override
        public HttpVersion protocolVersion() {
            return nettyResponse.protocolVersion();
        }

        @Override
        public void setProtocolVersion(HttpVersion httpVersion) {
            nettyResponse.setProtocolVersion(httpVersion);
        }

        @Override
        public HttpHeaders headers() {
            return nettyResponse.headers();
        }

        @Override
        public void header(CharSequence name, CharSequence value) {
            nettyResponse.headers().set(name, value);
        }

        @Override
        public MessageType messageType() {
            return MessageType.RESPONSE;
        }

        @Override
        public Protocol protocol() {
            return createHttpProtocol(nettyResponse.protocolVersion());
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        /**
         * 设置响应Body
         */
        public void setBody(String body) {
            ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            nettyResponse.content().clear().writeBytes(content);
            nettyResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        }

        /**
         * 获取响应Body
         */
        public String getBody() {
            if (nettyResponse.content().isReadable()) {
                return nettyResponse.content().toString(StandardCharsets.UTF_8);
            }
            return "";
        }

        /**
         * 获取底层Netty响应对象（内部使用）
         */
        FullHttpResponse getNettyResponse() {
            return nettyResponse;
        }
    }

    // ========== 工具方法 ==========

    /**
     * 根据HTTP版本创建协议对象
     */
    private static Protocol createHttpProtocol(HttpVersion version) {
        return new Protocol() {
            @Override
            public String type() {
                return "HTTP";
            }

            @Override
            public String getVersion() {
                return version.text();
            }

            @Override
            public boolean isConnectionOriented() {
                return true;
            }

            @Override
            public boolean isRequestResponseBased() {
                return true;
            }

            @Override
            public boolean isStreamingSupported() {
                return false;
            }
        };
    }

    /**
     * 设置响应Body内容
     *
     * @param body 响应体字符串
     */
    public void setResponseBody(String body) {
        HttpResponseMessage response = response();
        if (httpResponseAdapter != null) {
            httpResponseAdapter.setBody(body);
        }
    }

    /**
     * 获取响应Body内容
     *
     * @return 响应体字符串
     */
    public String getResponseBody() {
        HttpResponseMessage response = response();
        if (httpResponseAdapter != null) {
            return httpResponseAdapter.getBody();
        }
        return "";
    }

    /**
     * 获取请求Body内容
     *
     * @return 请求体字符串
     */
    public String getRequestBody() {
        HttpRequestMessage request = request();
        if (httpRequestAdapter != null) {
            return httpRequestAdapter.getBody();
        }
        return "";
    }

    /**
     * 获取请求Content-Type
     *
     * @return Content-Type头的值
     */
    public String getRequestContentType() {
        HttpRequestMessage request = request();
        if (httpRequestAdapter != null) {
            return httpRequestAdapter.getContentType();
        }
        return null;
    }

    @Override
    public String toString() {
        return "DefaultHttpServerExchange{" +
                "protocol=" + protocol +
                ", attributes=" + attributes +
                ", httpRequestAdapter=" + httpRequestAdapter +
                ", httpResponseAdapter=" + httpResponseAdapter +
                '}';
    }
}
