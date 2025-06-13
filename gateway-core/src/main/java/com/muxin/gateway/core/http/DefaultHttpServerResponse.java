package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import lombok.Data;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for FullHttpResponse to implement HttpServerResponse.
 * <p>
 * Provides basic implementation for response manipulation in a Netty-based gateway.
 *
 * @author Administrator
 * @date 2024/11/21 16:37
 */
@Data
public class DefaultHttpServerResponse implements HttpServerResponse {

    private final FullHttpResponse response;
    private final List<Cookie> cookies = new ArrayList<>();

    public DefaultHttpServerResponse() {
        this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // 设置默认头
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    public DefaultHttpServerResponse(HttpResponseStatus status) {
        this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    public DefaultHttpServerResponse(FullHttpResponse response) {
        this.response = response;
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        if (cookie != null) {
            cookies.add(cookie);
            // 更新响应头
            updateCookieHeaders();
        }
        return this;
    }

    @Override
    public HttpServerResponse addHeader(CharSequence name, CharSequence value) {
        if (name != null && value != null) {
            response.headers().add(name, value);
        }
        return this;
    }

    @Override
    public HttpServerResponse chunkedTransfer(boolean chunked) {
        if (chunked) {
            response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            response.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
        } else {
            response.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
        }
        return this;
    }

    @Override
    public HttpServerResponse compression(boolean compress) {
        if (compress) {
            response.headers().set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
        } else {
            response.headers().remove(HttpHeaderNames.CONTENT_ENCODING);
        }
        return this;
    }

    @Override
    public HttpServerResponse header(CharSequence name, CharSequence value) {
        if (name != null && value != null) {
            response.headers().set(name, value);
        }
        return this;
    }

    @Override
    public HttpServerResponse headers(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((name, value) -> response.headers().set(name, value));
        }
        return this;
    }

    @Override
    public HttpServerResponse keepAlive(boolean keepAlive) {
        HttpUtil.setKeepAlive(response, keepAlive);
        return this;
    }

    @Override
    public HttpServerResponse status(HttpResponseStatus status) {
        if (status != null) {
            response.setStatus(status);
        }
        return this;
    }

    @Override
    public HttpServerResponse status(int code) {
        response.setStatus(HttpResponseStatus.valueOf(code));
        return this;
    }

    @Override
    public HttpServerResponse body(String body) {
        if (body != null) {
            ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            response.content().clear().writeBytes(content);
            content.release();
            // 更新Content-Length
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return this;
    }

    @Override
    public HttpServerResponse body(byte[] body) {
        if (body != null) {
            response.content().clear().writeBytes(body);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length);
        }
        return this;
    }

    @Override
    public HttpServerResponse body(ByteBuf body) {
        if (body != null) {
            response.content().clear().writeBytes(body);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return this;
    }

    @Override
    public String getHeader(CharSequence name) {
        return name != null ? response.headers().get(name) : null;
    }

    @Override
    public boolean hasHeader(CharSequence name) {
        return name != null && response.headers().contains(name);
    }

    @Override
    public HttpHeaders responseHeaders() {
        return response.headers();
    }

    @Override
    public HttpResponseStatus status() {
        return response.status();
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<>(cookies);
    }

    @Override
    public ByteBuf content() {
        return response.content();
    }

    @Override
    public FullHttpResponse getNettyResponse() {
        // 确保Cookie头被正确设置
        updateCookieHeaders();
        return response;
    }

    /**
     * 更新Cookie响应头
     */
    private void updateCookieHeaders() {
        if (!cookies.isEmpty()) {
            response.headers().remove(HttpHeaderNames.SET_COOKIE);
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
    }

    /**
     * 创建空响应
     */
    public static DefaultHttpServerResponse empty() {
        return new DefaultHttpServerResponse();
    }

    /**
     * 创建带状态码的响应
     */
    public static DefaultHttpServerResponse of(HttpResponseStatus status) {
        return new DefaultHttpServerResponse(status);
    }

    /**
     * 创建JSON响应
     */
    public static DefaultHttpServerResponse json(String jsonBody) {
        DefaultHttpServerResponse response = new DefaultHttpServerResponse();
        response.header(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.body(jsonBody);
        return response;
    }

    /**
     * 创建文本响应
     */
    public static DefaultHttpServerResponse text(String textBody) {
        DefaultHttpServerResponse response = new DefaultHttpServerResponse();
        response.header(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.body(textBody);
        return response;
    }

    @Override
    public Map<CharSequence, List<Cookie>> allCookies() {
        return Map.of();
    }

    @Override
    public SocketAddress hostAddress() {
        return null;
    }

    @Override
    public SocketAddress connectionHostAddress() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public SocketAddress connectionRemoteAddress() {
        return null;
    }

    @Override
    public String scheme() {
        return "";
    }

    @Override
    public String connectionScheme() {
        return "";
    }

    @Override
    public String hostName() {
        return "";
    }

    @Override
    public int hostPort() {
        return 0;
    }

    @Override
    public String fullPath() {
        return "";
    }

    @Override
    public String requestId() {
        return "";
    }

    @Override
    public boolean isKeepAlive() {
        return false;
    }

    @Override
    public boolean isWebsocket() {
        return false;
    }

    @Override
    public HttpMethod method() {
        return null;
    }

    @Override
    public String uri() {
        return "";
    }

    @Override
    public HttpVersion version() {
        return null;
    }
}
