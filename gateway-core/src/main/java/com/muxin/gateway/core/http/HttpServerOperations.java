package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.Data;

import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  http请求操作类同时实现HttpServerRequest和HttpServerResponse
 *
 * @author Administrator
 * @date 2025/1/20 15:06
 */
@Data
public class HttpServerOperations implements HttpServerRequest, HttpServerResponse {

    private final Map<String, Object> attributes = new HashMap<>();

    private final FullHttpRequest request;

    private final ChannelHandlerContext ctx;

    private FullHttpResponse response;

    private String responseCode;

    private ZonedDateTime timestamp;



    public HttpServerOperations(FullHttpRequest request, ChannelHandlerContext ctx) {
        this.request = request;
        this.ctx = ctx;
        this.timestamp = ZonedDateTime.now();
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String param(CharSequence key) {
        return "";
    }

    @Override
    public Map<String, String> params() {
        return Map.of();
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
    public HttpHeaders requestHeaders() {
        return request.headers();
    }

    @Override
    public ZonedDateTime timestamp() {
        return timestamp;
    }

    @Override
    public ByteBuf reqBody() {
        return null;
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
    public HttpServerResponse headers(Map<String, String> headers) {
        return null;
    }

    @Override
    public HttpServerResponse keepAlive(boolean keepAlive) {
        return null;
    }

    @Override
    public Map<String, String> responseHeaders() {
        return Map.of();
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
    public ByteBuf resBody() {
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
