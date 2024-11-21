package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adapter for FullHttpResponse to implement HttpServerResponse.
 * <p>
 * Provides basic implementation for response manipulation in a Netty-based gateway.
 *
 * @author Administrator
 * @date 2024/11/21 16:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DefaultHttpServerResponse implements HttpServerResponse {


    private final HttpVersion version;

    private final HttpResponseStatus status;

    private final HttpHeaders headers;

    private final String body;

    private final long responseTime;


    public DefaultHttpServerResponse(HttpResponseStatus status, HttpHeaders headers, String body) {
        this(HttpVersion.HTTP_1_1, status, headers, body, System.currentTimeMillis());
    }


    public DefaultHttpServerResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers, String body) {
        this(version, status, headers, body, System.currentTimeMillis());
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        return null;
    }

    @Override
    public HttpServerResponse addHeader(CharSequence name, CharSequence value) {
        return null;
    }

    @Override
    public HttpServerResponse chunkedTransfer(boolean chunked) {
        return null;
    }

    @Override
    public HttpServerResponse compression(boolean compress) {
        return null;
    }

    @Override
    public HttpServerResponse header(CharSequence name, CharSequence value) {
        return null;
    }

    @Override
    public HttpServerResponse headers(HttpHeaders headers) {
        return null;
    }

    @Override
    public HttpServerResponse keepAlive(boolean keepAlive) {
        return null;
    }

    @Override
    public HttpHeaders responseHeaders() {
        return null;
    }

    @Override
    public HttpResponseStatus status() {
        return null;
    }

    @Override
    public HttpServerResponse status(HttpResponseStatus status) {
        return null;
    }

    @Override
    public HttpServerResponse trailerHeaders(Consumer<? super HttpHeaders> trailerHeaders) {
        return null;
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
