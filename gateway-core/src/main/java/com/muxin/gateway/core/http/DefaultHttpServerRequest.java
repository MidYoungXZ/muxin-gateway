package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 17:05
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DefaultHttpServerRequest implements HttpServerRequest {


    private FullHttpRequest request;

    private final String uri;

    private final long beginTime;

    private final String remoteAddress;

    private final String host;

    private final String requestId;


    @Override
    public String param(CharSequence key) {
        return "";
    }

    @Override
    public Map<String, String> params() {
        return Map.of();
    }

    @Override
    public HttpServerRequest paramsResolver(Function<? super String, Map<String, String>> paramsResolver) {
        return null;
    }

    @Override
    public boolean isFormUrlencoded() {
        return false;
    }

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public InetSocketAddress hostAddress() {
        return null;
    }

    @Override
    public SocketAddress connectionHostAddress() {
        return null;
    }

    @Override
    public InetSocketAddress remoteAddress() {
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
    public HttpHeaders requestHeaders() {
        return null;
    }

    @Override
    public ZonedDateTime timestamp() {
        return null;
    }

    @Override
    public ByteBuf body() {
        return request.content();
    }

    @Override
    public Map<CharSequence, List<Cookie>> allCookies() {
        return Map.of();
    }

    @Override
    public String fullPath() {
        return request.uri();
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
        return request.method();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public HttpVersion version() {
        return request.protocolVersion();
    }
}
