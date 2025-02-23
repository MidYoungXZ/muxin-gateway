package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;

/**
 * 定义了一个处理HTTP服务器响应的接口。该接口提供了设置和获取响应头、状态码、Cookie、压缩、传输编码以及响应体的方法。
 * 所有具体的HTTP响应对象都需要实现这个接口。
 *
 * @author Stephane Maldini
 * @since 0.5
 */
public interface HttpServerResponse extends HttpServerInfos, AttributesHolder {

    /**
     * 添加一个出站Cookie。
     *
     * @param cookie 要添加的Cookie
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse addCookie(Cookie cookie);

    /**
     * 添加一个出站HTTP头，如果该头已经存在，则追加值。
     *
     * @param name  头名称
     * @param value 头值
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse addHeader(CharSequence name, CharSequence value);

    /**
     * 设置 Transfer-Encoding 头。
     *
     * @param chunked true 如果 Transfer-Encoding: chunked
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse chunkedTransfer(boolean chunked);

    /**
     * 启用/禁用底层响应的压缩处理（gzip/deflate）。
     *
     * @param compress 是否启用压缩
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse compression(boolean compress);

    /**
     * 设置一个出站HTTP头，如果该头已经存在，则替换其值。
     *
     * @param name  头名称
     * @param value 头值
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse header(CharSequence name, CharSequence value);

    /**
     * 设置出站HTTP头，如果这些头已经存在，则替换其值。
     *
     * @param headers Netty 头映射
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse headers(Map<String, String> headers);

    /**
     * 设置请求的 keepAlive 状态。如果为 true，则设置 keepAlive 头；否则移除现有的 keepAlive 头。
     *
     * @param keepAlive 是否启用 keepAlive
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse keepAlive(boolean keepAlive);

    /**
     * 返回将发送给客户端的出站HTTP头。
     *
     * @return 发送给客户端的头映射
     */
    Map<String, String> responseHeaders();

    /**
     * 返回已分配的HTTP状态码。
     *
     * @return 已分配的HTTP状态码
     */
    HttpResponseStatus status();

    /**
     * 设置将与头一起发送的HTTP状态码。
     *
     * @param status 要发送的HTTP状态码
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    HttpServerResponse status(HttpResponseStatus status);

    /**
     * 设置将与头一起发送的HTTP状态码。
     *
     * @param status 要发送的HTTP状态码
     * @return 当前的 {@link HttpServerResponse} 对象
     */
    default HttpServerResponse status(int status) {
        return status(HttpResponseStatus.valueOf(status));
    }

    /**
     * 获取响应体。
     *
     * @return 响应体的 {@link ByteBuf}
     */
    ByteBuf resBody();
}
