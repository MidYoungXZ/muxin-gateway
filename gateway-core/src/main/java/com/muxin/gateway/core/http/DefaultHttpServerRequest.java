package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.AllArgsConstructor;
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
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DefaultHttpServerRequest implements HttpServerRequest {

    private final HttpVersion version;

    private final HttpMethod method;

    private final HttpResponseStatus status;

    private final HttpHeaders headers;

    private final String uri;

    private final String body;

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
    public String protocol() {
        return "";
    }

    @Override
    public ZonedDateTime timestamp() {
        return null;
    }

    @Override
    public Map<CharSequence, List<Cookie>> allCookies() {
        return Map.of();
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
