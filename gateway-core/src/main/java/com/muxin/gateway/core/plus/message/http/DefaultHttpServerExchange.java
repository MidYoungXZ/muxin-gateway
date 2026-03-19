package com.muxin.gateway.core.plus.message.http;

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
 * 优化：直接使用原始请求，避免不必要的复制
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultHttpServerExchange implements HttpServerExchange {

    private final Map<String, Object> attributes;
    private final NettyHttpRequestAdapter requestAdapter;
    private volatile NettyHttpResponseAdapter responseAdapter;

    public DefaultHttpServerExchange(FullHttpRequest request) {
        Objects.requireNonNull(request, "HTTP请求不能为空");
        this.attributes = new HashMap<>();
        this.requestAdapter = new NettyHttpRequestAdapter(request);
        log.debug("创建HTTP服务器交换对象: {} {}", request.method(), request.uri());
    }

    @Override
    public HttpRequestMessage request() {
        return requestAdapter;
    }

    @Override
    public HttpResponseMessage response() {
        return responseAdapter;
    }

    @Override
    public void setResponse(HttpResponseMessage response) {
        if (response instanceof NettyHttpResponseAdapter) {
            this.responseAdapter = (NettyHttpResponseAdapter) response;
        } else {
            throw new IllegalArgumentException("不支持的响应类型: " + response.getClass());
        }
    }

    @Override
    public FullHttpRequest nettyRequest() {
        return requestAdapter.getNettyRequest();
    }

    @Override
    public FullHttpResponse nettyResponse() {
        return responseAdapter != null ? responseAdapter.getNettyResponse() : null;
    }

    @Override
    public void setNettyResponse(FullHttpResponse response) {
        this.responseAdapter = new NettyHttpResponseAdapter(response);
    }

    @Override
    public String getRequestBody() {
        return requestAdapter.getBody();
    }

    @Override
    public void setResponseBody(String body) {
        if (responseAdapter != null) {
                responseAdapter.setBody(body);
            }
    }

    @Override
    public String getResponseBody() {
        return responseAdapter != null ? responseAdapter.getBody() : "";
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "DefaultHttpServerExchange{" +
                "request=" + requestAdapter.method() + " " + requestAdapter.uri() +
                ", hasResponse=" + (responseAdapter != null) +
                '}';
    }

    private static class NettyHttpRequestAdapter implements HttpRequestMessage {

        private final FullHttpRequest nettyRequest;
        private final String requestId;
        private final ZonedDateTime timestamp;

        public NettyHttpRequestAdapter(FullHttpRequest nettyRequest) {
                this.nettyRequest = nettyRequest;
                this.requestId = System.currentTimeMillis() + "-" + System.nanoTime() % 10000;
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
            Map<String, String> params = new HashMap<>();
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
        public Map<String, Object> getAttributes() {
                return new HashMap<>();
        }

        public String getBody() {
                if (nettyRequest.content().isReadable()) {
                    return nettyRequest.content().toString(StandardCharsets.UTF_8);
                }
                return "";
        }

        public String getContentType() {
                return nettyRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
            }

        FullHttpRequest getNettyRequest() {
                return nettyRequest;
            }
        }

        private static class NettyHttpResponseAdapter implements HttpResponseMessage {

            private final FullHttpResponse nettyResponse;
            private final Map<String, Object> attributes;

            public NettyHttpResponseAdapter(FullHttpResponse nettyResponse) {
                this(nettyResponse, new HashMap<>());
            }

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
            public Map<String, Object> getAttributes() {
                return attributes;
            }

            public void setBody(String body) {
                ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
                nettyResponse.content().clear().writeBytes(content);
                nettyResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            }

            public String getBody() {
                if (nettyResponse.content().isReadable()) {
                    return nettyResponse.content().toString(StandardCharsets.UTF_8);
                }
                return "";
            }

            FullHttpResponse getNettyResponse() {
                return nettyResponse;
            }
        }
    }
