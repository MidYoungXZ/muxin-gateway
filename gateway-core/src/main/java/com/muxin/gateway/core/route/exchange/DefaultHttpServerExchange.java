package com.muxin.gateway.core.route.exchange;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP服务器交换对象的默认实现
 * 
 * 设计要点：
 * 1. 原始请求保持不变，通过 mutableRequest 存储修改后的副本
 * 2. mutate() 懒加载创建副本，避免不必要的复制
 * 3. 所有修改方法自动触发 mutate()
 *
 * @author muxin
 * @version 3.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultHttpServerExchange implements HttpServerExchange {

    private final FullHttpRequest originalRequest;
    private FullHttpRequest mutableRequest;
    private FullHttpResponse response;
    private final Map<String, Object> attributes;
    private final String requestId;
    private final ZonedDateTime timestamp;
    private final String remoteAddress;

    private volatile String cachedUri;
    private volatile String cachedPath;
    private volatile Map<String, String> cachedParams;

    public DefaultHttpServerExchange(FullHttpRequest request) {
        this(request, null);
    }

    public DefaultHttpServerExchange(FullHttpRequest request, String remoteAddress) {
        Objects.requireNonNull(request, "HTTP请求不能为空");
        this.originalRequest = request;
        this.mutableRequest = null;
        this.attributes = new ConcurrentHashMap<>();
        this.requestId = System.currentTimeMillis() + "-" + System.nanoTime() % 10000;
        this.timestamp = ZonedDateTime.now();
        this.remoteAddress = remoteAddress;
        cacheUriState(request.uri());
        if (log.isDebugEnabled()) {
            log.debug("创建HTTP服务器交换对象: {} {}", request.method(), request.uri());
        }
    }

    // ==================== 请求信息（只读）====================

    private FullHttpRequest currentRequest() {
        return mutableRequest != null ? mutableRequest : originalRequest;
    }

    private void cacheUriState(String uri) {
        this.cachedUri = uri;
        int queryIndex = uri.indexOf('?');
        this.cachedPath = queryIndex >= 0 ? uri.substring(0, queryIndex) : uri;
        if (queryIndex >= 0) {
            Map<String, String> params = new HashMap<>();
            String queryString = uri.substring(queryIndex + 1);
            for (String pair : queryString.split("&")) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    params.put(pair.substring(0, eq), pair.substring(eq + 1));
                } else if (!pair.isEmpty()) {
                    params.put(pair, "");
                }
            }
            this.cachedParams = params;
        } else {
            this.cachedParams = Collections.emptyMap();
        }
    }

    private void invalidateUriCache() {
        String currentUri = currentRequest().uri();
        if (!currentUri.equals(cachedUri)) {
            cacheUriState(currentUri);
        }
    }

    @Override
    public String method() {
        return currentRequest().method().name();
    }

    @Override
    public String uri() {
        return currentRequest().uri();
    }

    @Override
    public String fullPath() {
        invalidateUriCache();
        return cachedPath;
    }

    @Override
    public HttpHeaders headers() {
        return currentRequest().headers();
    }

    @Override
    public String header(CharSequence name) {
        return currentRequest().headers().get(name);
    }

    @Override
    public String requestId() {
        return requestId;
    }

    @Override
    public boolean isKeepAlive() {
        return HttpUtil.isKeepAlive(currentRequest());
    }

    @Override
    public String param(CharSequence key) {
        invalidateUriCache();
        return cachedParams.get(key.toString());
    }

    @Override
    public Map<String, String> params() {
        invalidateUriCache();
        return new HashMap<>(cachedParams);
    }

    @Override
    public String getRequestBody() {
        ByteBuf content = currentRequest().content();
        if (content != null && content.isReadable()) {
            return content.toString(StandardCharsets.UTF_8);
        }
        return "";
    }

    @Override
    public ZonedDateTime timestamp() {
        return timestamp;
    }

    @Override
    public String remoteAddress() {
        return remoteAddress;
    }

    // ==================== 响应信息 ====================

    @Override
    public HttpResponseStatus status() {
        return response != null ? response.status() : null;
    }

    @Override
    public HttpHeaders responseHeaders() {
        return response != null ? response.headers() : null;
    }

    @Override
    public String getResponseBody() {
        if (response != null && response.content().isReadable()) {
            return response.content().toString(StandardCharsets.UTF_8);
        }
        return "";
    }

    @Override
    public boolean hasResponse() {
        return response != null;
    }

    // ==================== 修改请求（创建副本）====================

    @Override
    public HttpServerExchange mutate() {
        if (mutableRequest == null) {
            ByteBuf content = originalRequest.content();
            ByteBuf copiedContent = content != null && content.isReadable()
                    ? Unpooled.copiedBuffer(content)
                    : Unpooled.buffer(0);

            mutableRequest = new DefaultFullHttpRequest(
                    originalRequest.protocolVersion(),
                    originalRequest.method(),
                    originalRequest.uri(),
                    copiedContent,
                    originalRequest.headers().copy(),
                    originalRequest.trailingHeaders().copy()
            );

            if (log.isDebugEnabled()) {
                log.debug("创建请求副本: {} {}", originalRequest.uri(), mutableRequest.uri());
            }
        }
        return this;
    }

    @Override
    public HttpServerExchange uri(String uri) {
        mutate();
        mutableRequest.setUri(uri);
        if (log.isDebugEnabled()) {
            log.debug("修改URI: {} -> {}", originalRequest.uri(), uri);
        }
        return this;
    }

    @Override
    public HttpServerExchange method(HttpMethod method) {
        mutate();
        mutableRequest.setMethod(method);
        return this;
    }

    @Override
    public HttpServerExchange header(CharSequence name, CharSequence value) {
        mutate();
        mutableRequest.headers().set(name, value);
        return this;
    }

    @Override
    public HttpServerExchange addHeader(CharSequence name, CharSequence value) {
        mutate();
        mutableRequest.headers().add(name, value);
        return this;
    }

    @Override
    public HttpServerExchange removeHeader(CharSequence name) {
        mutate();
        mutableRequest.headers().remove(name);
        return this;
    }

    @Override
    public boolean isMutated() {
        return mutableRequest != null;
    }

    // ==================== 响应设置 ====================

    @Override
    public void setStatus(HttpResponseStatus status) {
        if (response != null) {
            response.setStatus(status);
        } else {
            log.warn("[Exchange] setStatus called but response is null, requestId={}", requestId);
        }
    }

    @Override
    public void setResponseHeader(CharSequence name, CharSequence value) {
        if (response != null) {
            response.headers().set(name, value);
        } else {
            log.warn("[Exchange] setResponseHeader called but response is null, requestId={}", requestId);
        }
    }

    @Override
    public void setResponseBody(String body) {
        if (response != null && body != null) {
            ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            response.content().clear().writeBytes(content);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        }
    }

    @Override
    public void keepAlive(boolean keepAlive) {
        if (response != null) {
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // ==================== 内部方法（包级别可见）====================

    @Override
    public FullHttpRequest _nettyRequest() {
        return currentRequest();
    }

    @Override
    public FullHttpResponse _nettyResponse() {
        return response;
    }

    @Override
    public void _setNettyResponse(FullHttpResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "DefaultHttpServerExchange{" +
                "method=" + method() +
                ", uri=" + uri() +
                ", requestId=" + requestId +
                ", mutated=" + isMutated() +
                ", hasResponse=" + hasResponse() +
                '}';
    }
}
