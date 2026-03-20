package com.muxin.gateway.core.route.exchange;

import io.netty.handler.codec.http.*;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * HTTP服务器交换接口
 * 提供请求/响应访问和修改能力
 * 
 * 设计原则：
 * 1. 原始请求保持不变，通过 mutate() 创建副本进行修改
 * 2. 隐藏 Netty 实现细节，只暴露必要的方法
 * 3. 链式调用支持
 *
 * @author muxin
 * @version 3.0.0
 * @since 1.0.0
 */
public interface HttpServerExchange {

    // ==================== 请求信息（只读）====================

    /**
     * 获取请求方法
     */
    String method();

    /**
     * 获取请求URI（包含查询参数）
     */
    String uri();

    /**
     * 获取请求路径（不含查询参数）
     */
    String fullPath();

    /**
     * 获取请求头
     */
    HttpHeaders headers();

    /**
     * 获取指定请求头的值
     */
    String header(CharSequence name);

    /**
     * 获取请求ID
     */
    String requestId();

    /**
     * 是否保持连接
     */
    boolean isKeepAlive();

    /**
     * 获取指定查询参数的值
     */
    String param(CharSequence key);

    /**
     * 获取所有查询参数
     */
    Map<String, String> params();

    /**
     * 获取请求体内容
     */
    String getRequestBody();

    /**
     * 获取请求时间戳
     */
    ZonedDateTime timestamp();

    // ==================== 响应信息 ====================

    /**
     * 获取响应状态码
     */
    HttpResponseStatus status();

    /**
     * 获取响应头
     */
    HttpHeaders responseHeaders();

    /**
     * 获取响应体内容
     */
    String getResponseBody();

    /**
     * 是否已有响应
     */
    boolean hasResponse();

    // ==================== 修改请求（创建副本）====================

    /**
     * 创建请求副本，后续修改都在副本上进行
     * @return this，支持链式调用
     */
    HttpServerExchange mutate();

    /**
     * 修改请求URI（会自动调用 mutate()）
     */
    HttpServerExchange uri(String uri);

    /**
     * 设置请求方法（会自动调用 mutate()）
     */
    HttpServerExchange method(HttpMethod method);

    /**
     * 设置请求头（会自动调用 mutate()）
     */
    HttpServerExchange header(CharSequence name, CharSequence value);

    /**
     * 添加请求头（会自动调用 mutate()）
     */
    HttpServerExchange addHeader(CharSequence name, CharSequence value);

    /**
     * 移除请求头（会自动调用 mutate()）
     */
    HttpServerExchange removeHeader(CharSequence name);

    /**
     * 是否已创建修改副本
     */
    boolean isMutated();

    // ==================== 响应设置 ====================

    /**
     * 设置响应状态码
     */
    void setStatus(HttpResponseStatus status);

    /**
     * 设置响应头
     */
    void setResponseHeader(CharSequence name, CharSequence value);

    /**
     * 设置响应体
     */
    void setResponseBody(String body);

    /**
     * 设置连接保持
     */
    void keepAlive(boolean keepAlive);

    // ==================== 属性存储 ====================

    /**
     * 设置属性
     */
    void setAttribute(String key, Object value);

    /**
     * 获取属性
     */
    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String key) {
        return (T) getAttributes().get(key);
    }

    /**
     * 获取所有属性
     */
    Map<String, Object> getAttributes();

    // ==================== 内部方法（包级别可见）====================
    // 由 GatewayProcessor 等核心组件使用，Filter 不应使用

    /**
     * 获取 Netty 原生请求对象（内部使用）
     */
    default FullHttpRequest _nettyRequest() {
        throw new UnsupportedOperationException("内部方法，仅限包内使用");
    }

    /**
     * 获取 Netty 原生响应对象（内部使用）
     */
    default FullHttpResponse _nettyResponse() {
        throw new UnsupportedOperationException("内部方法，仅限包内使用");
    }

    /**
     * 设置 Netty 原生响应对象（内部使用）
     */
    default void _setNettyResponse(FullHttpResponse response) {
        throw new UnsupportedOperationException("内部方法，仅限包内使用");
    }
}