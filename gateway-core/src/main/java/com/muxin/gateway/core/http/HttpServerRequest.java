package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * 定义了一个处理HTTP服务器请求的接口。该接口提供了访问请求参数、请求头、请求体和请求时间戳的方法。
 * 所有具体的HTTP请求对象都需要实现这个接口。
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface HttpServerRequest extends HttpServerInfos {

    /**
     * 获取通过 {@code {}} 捕获的URI参数，例如 {@code /test/{param}}。
     *
     * @param key 参数名称，例如 {@code "param"} 在URI {@code /test/{param}} 中
     * @return 捕获的参数值
     */
    String param(CharSequence key);

    /**
     * 返回所有通过 {@code {}} 捕获的URI参数，例如 {@code /test/{param1}/{param2}}，以键值对的形式。
     *
     * @return 捕获的参数键值对映射
     */
    Map<String, String> params();

    /**
     * 返回请求的 {@code Content-Type} 是否为 {@code application/x-www-form-urlencoded}。
     *
     * @return 如果请求的 {@code Content-Type} 为 {@code application/x-www-form-urlencoded}，则返回 true，否则返回 false
     * @since 1.0.11
     */
    boolean isFormUrlencoded();

    /**
     * 返回请求的 {@code Content-Type} 是否为 {@code multipart/form-data}。
     *
     * @return 如果请求的 {@code Content-Type} 为 {@code multipart/form-data}，则返回 true，否则返回 false
     * @since 1.0.11
     */
    boolean isMultipart();

    /**
     * 返回入站的 {@link HttpHeaders}。
     *
     * @return 入站的 {@link HttpHeaders}
     */
    HttpHeaders requestHeaders();

    /**
     * 返回请求接收的时间。
     *
     * @return 请求接收的时间
     * @since 1.0.28
     */
    ZonedDateTime timestamp();

    /**
     * 返回请求体。
     *
     * @return 请求体的 {@link ByteBuf}
     */
    ByteBuf reqBody();
}
