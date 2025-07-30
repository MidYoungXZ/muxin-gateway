package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 统一的HTTP交换实现类，直接实现所有请求和响应功能
 * 不再使用委托模式，所有功能都在此类中实现
 *
 * @author Administrator
 * @date 2024/11/20 15:54
 */
@Data
@Builder
@AllArgsConstructor
public class DefaultServerWebExchange implements ServerWebExchange {

    // ========== 核心属性 ==========
    private ChannelHandlerContext ctx;
    private ProcessingPhase phase;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    // ========== 请求相关属性 ==========
    private FullHttpRequest request;
    private String uri;
    private long beginTime;
    private String remoteAddress;
    private String host;
    private String requestId;

    // 缓存解析的参数
    private Map<String, String> pathParams;
    private Map<CharSequence, List<Cookie>> requestCookies;
    private Function<? super String, Map<String, String>> paramsResolver;

    // ========== 响应相关属性 ==========
    private FullHttpResponse response;

    private final List<Cookie> responseCookies = new ArrayList<>();

    @Builder.Default
    private boolean cookiesUpdated = false;

    // ========== 构造器和初始化 ==========

    public DefaultServerWebExchange() {
        initDefaults();
    }

    private void initDefaults() {
        if (this.response == null) {
            this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            this.response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        }
        if (this.beginTime == 0) {
            this.beginTime = System.currentTimeMillis();
        }
        if (this.requestId == null) {
            this.requestId = UUID.randomUUID().toString();
        }
    }

    public DefaultServerWebExchange(FullHttpRequest request, ChannelHandlerContext ctx) {
        this();
        this.request = request;
        this.ctx = ctx;
        this.uri = request.uri();
        this.host = request.headers().get(HttpHeaderNames.HOST);
        this.remoteAddress = ctx != null ? ctx.channel().remoteAddress().toString() : "127.0.0.1:0";
        this.phase = new ProcessingPhase().running();
    }

    // ========== ServerWebExchange 方法 ==========

    @Override
    public HttpServerRequest getRequest() {
        // 为了向后兼容，返回this（因为this实现了HttpServerRequest）
        return this;
    }

    @Override
    public HttpServerResponse getResponse() {
        // 为了向后兼容，返回this（因为this实现了HttpServerResponse）
        return this;
    }

    @Override
    public void setOriginalResponse(Object response) {
        if (response instanceof FullHttpResponse httpResponse) {
            this.response = httpResponse;
        }
    }

    @Override
    public void setOriginalRequest(Object request) {
        if (request instanceof FullHttpRequest httpRequest) {
            this.request = httpRequest;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOriginalResponse() {
        // 确保Cookie头被正确设置
        if (!cookiesUpdated) {
            updateCookieHeaders();
        }
        return (T) response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOriginalRequest() {
        return (T) request;
    }


    @Override
    public ChannelHandlerContext inboundContext() {
        return ctx;
    }

    @Override
    public ProcessingPhase processingPhase() {
        return phase;
    }

    @Override
    public void setProcessingPhase(ProcessingPhase phase) {
        this.phase = phase;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // ========== HttpServerRequest 方法实现 ==========

    @Override
    public String param(CharSequence key) {
        if (key == null) return null;
        Map<String, String> params = params();
        return params.get(key.toString());
    }

    @Override
    public Map<String, String> params() {
        if (pathParams == null) {
            pathParams = new HashMap<>();
            if (paramsResolver != null && uri != null) {
                pathParams = paramsResolver.apply(uri);
            }
        }
        return pathParams;
    }

    @Override
    public HttpServerRequest paramsResolver(Function<? super String, Map<String, String>> paramsResolver) {
        this.paramsResolver = paramsResolver;
        return this;
    }

    @Override
    public boolean isFormUrlencoded() {
        String contentType = getContentType();
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }

    @Override
    public boolean isMultipart() {
        String contentType = getContentType();
        return contentType != null && contentType.contains("multipart/form-data");
    }

    private String getContentType() {
        HttpHeaders headers = requestHeaders();
        return headers != null ? headers.get(HttpHeaderNames.CONTENT_TYPE) : null;
    }

    @Override
    public HttpHeaders requestHeaders() {
        return request != null ? request.headers() : null;
    }

    @Override
    public ZonedDateTime timestamp() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(beginTime), ZoneId.systemDefault());
    }

    @Override
    public ByteBuf body() {
        return request != null ? request.content() : null;
    }

    @Override
    public void release() {
        if (request != null && request.refCnt() > 0) {
            request.release();
        }
    }

    // ========== HttpServerResponse 方法实现 ==========

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        if (cookie != null) {
            responseCookies.add(cookie);
            // 立即更新响应头，确保修改映射到FullHttpResponse
            updateCookieHeaders();
        }
        return this;
    }

    @Override
    public HttpServerResponse addHeader(CharSequence name, CharSequence value) {
        if (name != null && value != null && response != null) {
            response.headers().add(name, value);
        }
        return this;
    }

    @Override
    public HttpServerResponse chunkedTransfer(boolean chunked) {
        if (response != null) {
            if (chunked) {
                response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                response.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
            } else {
                response.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            }
        }
        return this;
    }

    @Override
    public HttpServerResponse compression(boolean compress) {
        if (response != null) {
            if (compress) {
                response.headers().set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
            } else {
                response.headers().remove(HttpHeaderNames.CONTENT_ENCODING);
            }
        }
        return this;
    }

    @Override
    public HttpServerResponse header(CharSequence name, CharSequence value) {
        if (name != null && value != null && response != null) {
            response.headers().set(name, value);
        }
        return this;
    }

    @Override
    public HttpServerResponse headers(Map<String, String> headers) {
        if (headers != null && response != null) {
            headers.forEach((name, value) -> response.headers().set(name, value));
        }
        return this;
    }

    @Override
    public HttpServerResponse keepAlive(boolean keepAlive) {
        if (response != null) {
            HttpUtil.setKeepAlive(response, keepAlive);
        }
        return this;
    }

    @Override
    public HttpServerResponse status(HttpResponseStatus status) {
        if (status != null && response != null) {
            response.setStatus(status);
        }
        return this;
    }

    @Override
    public HttpServerResponse status(int status) {
        if (response != null) {
            response.setStatus(HttpResponseStatus.valueOf(status));
        }
        return this;
    }

    @Override
    public HttpServerResponse body(String body) {
        if (body != null && response != null) {
            ByteBuf content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            try {
                response.content().clear().writeBytes(content);
                // 更新Content-Length
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            } finally {
                content.release();
            }
        }
        return this;
    }

    @Override
    public HttpServerResponse body(byte[] body) {
        if (body != null && response != null) {
            response.content().clear().writeBytes(body);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length);
        }
        return this;
    }

    @Override
    public HttpServerResponse body(ByteBuf body) {
        if (body != null && response != null) {
            response.content().clear().writeBytes(body);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        }
        return this;
    }

    @Override
    public String getHeader(CharSequence name) {
        return (name != null && response != null) ? response.headers().get(name) : null;
    }

    @Override
    public boolean hasHeader(CharSequence name) {
        return name != null && response != null && response.headers().contains(name);
    }

    @Override
    public HttpHeaders responseHeaders() {
        return response != null ? response.headers() : null;
    }

    @Override
    public HttpResponseStatus status() {
        return response != null ? response.status() : null;
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<>(responseCookies);
    }

    @Override
    public ByteBuf content() {
        return response != null ? response.content() : null;
    }


    /**
     * 更新Cookie响应头，确保修改映射到FullHttpResponse
     */
    private void updateCookieHeaders() {
        if (!responseCookies.isEmpty() && response != null) {
            response.headers().remove(HttpHeaderNames.SET_COOKIE);
            for (Cookie cookie : responseCookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        cookiesUpdated = true;
    }

    // ========== HttpServerInfos 方法实现 ==========

    @Override
    public Map<CharSequence, List<Cookie>> allCookies() {
        if (requestCookies == null) {
            requestCookies = new HashMap<>();
            HttpHeaders headers = requestHeaders();
            if (headers != null) {
                String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
                if (cookieHeader != null) {
                    Set<Cookie> decodedCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
                    for (Cookie cookie : decodedCookies) {
                        requestCookies.computeIfAbsent(cookie.name(), k -> new ArrayList<>()).add(cookie);
                    }
                }
            }
        }
        return requestCookies;
    }

    @Override
    public SocketAddress hostAddress() {
        return parseHost();
    }

    @Override
    public SocketAddress connectionHostAddress() {
        return parseHost();
    }

    @Override
    public SocketAddress remoteAddress() {
        return parseRemoteAddress();
    }

    @Override
    public SocketAddress connectionRemoteAddress() {
        return parseRemoteAddress();
    }

    @Override
    public String scheme() {
        // 从请求头判断协议，默认http
        HttpHeaders headers = requestHeaders();
        if (headers != null) {
            String proto = headers.get("X-Forwarded-Proto");
            if (proto != null) {
                return proto.toLowerCase();
            }
        }
        return "http";
    }

    @Override
    public String connectionScheme() {
        return scheme();
    }

    @Override
    public String hostName() {
        if (host != null && host.contains(":")) {
            return host.substring(0, host.indexOf(":"));
        }
        return host != null ? host : "localhost";
    }

    @Override
    public int hostPort() {
        if (host != null && host.contains(":")) {
            try {
                return Integer.parseInt(host.substring(host.indexOf(":") + 1));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return "https".equals(scheme()) ? 443 : 80;
    }

    @Override
    public String fullPath() {
        return request != null ? request.uri() : (uri != null ? uri : "");
    }

    @Override
    public String requestId() {
        return requestId != null ? requestId : "";
    }

    @Override
    public boolean isKeepAlive() {
        if (request != null) {
            return HttpUtil.isKeepAlive(request);
        }
        return response != null && HttpUtil.isKeepAlive(response);
    }

    @Override
    public boolean isWebsocket() {
        HttpHeaders headers = requestHeaders();
        if (headers != null) {
            String upgrade = headers.get(HttpHeaderNames.UPGRADE);
            return "websocket".equalsIgnoreCase(upgrade);
        }
        return false;
    }

    @Override
    public HttpMethod method() {
        return request != null ? request.method() : HttpMethod.GET;
    }

    @Override
    public String uri() {
        return request != null ? request.uri() : (uri != null ? uri : "");
    }

    @Override
    public HttpVersion version() {
        if (request != null) {
            return request.protocolVersion();
        }
        return response != null ? response.protocolVersion() : HttpVersion.HTTP_1_1;
    }

    // ========== 辅助方法 ==========

    private InetSocketAddress parseHost() {
        return new InetSocketAddress(hostName(), hostPort());
    }

    private InetSocketAddress parseRemoteAddress() {
        if (remoteAddress != null && remoteAddress.contains(":")) {
            try {
                String[] parts = remoteAddress.split(":");
                if (parts.length >= 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[parts.length - 1]);
                    return new InetSocketAddress(ip, port);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 从Netty请求创建交换对象
     */
    public static DefaultServerWebExchange fromNettyRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        return new DefaultServerWebExchange(request, ctx);
    }

    /**
     * 创建空的交换对象
     */
    public static DefaultServerWebExchange empty() {
        return new DefaultServerWebExchange();
    }

    /**
     * 创建带状态码的交换对象
     */
    public static DefaultServerWebExchange withStatus(HttpResponseStatus status) {
        DefaultServerWebExchange exchange = new DefaultServerWebExchange();
        exchange.status(status);
        return exchange;
    }

    /**
     * 创建JSON响应交换对象
     */
    public static DefaultServerWebExchange jsonResponse(String jsonBody) {
        DefaultServerWebExchange exchange = new DefaultServerWebExchange();
        exchange.header(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        exchange.body(jsonBody);
        return exchange;
    }

    /**
     * 创建文本响应交换对象
     */
    public static DefaultServerWebExchange textResponse(String textBody) {
        DefaultServerWebExchange exchange = new DefaultServerWebExchange();
        exchange.header(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        exchange.body(textBody);
        return exchange;
    }

    // ========== 兼容性方法（已废弃的静态方法） ==========

    /**
     * @deprecated 使用 fromNettyRequest 替代
     */
    @Deprecated
    public static DefaultServerWebExchange create(HttpServerRequest request, HttpServerResponse response,
                                                  ChannelHandlerContext ctx) {
        // 为了向后兼容，尝试从参数中提取信息
        DefaultServerWebExchange exchange = new DefaultServerWebExchange();
        exchange.ctx = ctx;
        exchange.phase = new ProcessingPhase().running();

        // 如果request是DefaultServerWebExchange类型，复制其属性
        if (request instanceof DefaultServerWebExchange) {
            DefaultServerWebExchange reqExchange = (DefaultServerWebExchange) request;
            exchange.request = reqExchange.request;
            exchange.uri = reqExchange.uri;
            exchange.beginTime = reqExchange.beginTime;
            exchange.remoteAddress = reqExchange.remoteAddress;
            exchange.host = reqExchange.host;
            exchange.requestId = reqExchange.requestId;
        }

        // 如果response是DefaultServerWebExchange类型，复制其响应属性
        if (response instanceof DefaultServerWebExchange) {
            DefaultServerWebExchange respExchange = (DefaultServerWebExchange) response;
            exchange.response = respExchange.response;
            exchange.responseCookies.addAll(respExchange.responseCookies);
        }

        return exchange;
    }

    /**
     * @deprecated 使用 fromNettyRequest 替代
     */
    @Deprecated
    public static DefaultServerWebExchange fromRequest(HttpServerRequest request, ChannelHandlerContext ctx) {
        DefaultServerWebExchange exchange = new DefaultServerWebExchange();
        exchange.ctx = ctx;
        exchange.phase = new ProcessingPhase().running();

        if (request instanceof DefaultServerWebExchange) {
            DefaultServerWebExchange reqExchange = (DefaultServerWebExchange) request;
            exchange.request = reqExchange.request;
            exchange.uri = reqExchange.uri;
            exchange.beginTime = reqExchange.beginTime;
            exchange.remoteAddress = reqExchange.remoteAddress;
            exchange.host = reqExchange.host;
            exchange.requestId = reqExchange.requestId;
        }

        return exchange;
    }

    @Override
    public String toString() {
        return "DefaultServerWebExchange{" +
                "ctx=" + ctx +
                ", phase=" + phase +
                ", attributes=" + attributes +
                ", request=" + request +
                ", uri='" + uri + '\'' +
                ", beginTime=" + beginTime +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", host='" + host + '\'' +
                ", requestId='" + requestId + '\'' +
                ", pathParams=" + pathParams +
                ", requestCookies=" + requestCookies +
                ", paramsResolver=" + paramsResolver +
                ", response=" + response +
                ", responseCookies=" + responseCookies +
                ", cookiesUpdated=" + cookiesUpdated +
                '}';
    }
}
