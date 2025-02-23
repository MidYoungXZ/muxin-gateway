package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 定义了一个HTTP信息接口，提供了访问HTTP流相关信息的方法，包括头信息、参数、URI、方法、WebSocket等。
 *
 * @author Stephane Maldini
 * @since 0.5
 */
public interface HttpInfos {

    /**
     * 返回从 {@link #uri()} 解码后的路径部分。
     *
     * @return 从 {@link #uri()} 解码后的路径部分
     * @since 0.9.6
     */
    String fullPath();

    /**
     * 返回请求的唯一标识符。该标识符由底层连接的ID和该连接上接收到的请求序列号组成。
     * <p>标识符格式:
     * {@code <CONNECTION_ID>-<REQUEST_NUMBER>}
     * </p>
     * <p>
     * 示例:
     * {@code
     *     <CONNECTION_ID>: 329c6ffd
     *     <REQUEST_NUMBER>: 5
     *
     *     结果: 329c6ffd-5
     * }
     * </p>
     *
     * @return 请求的唯一标识符
     * @since 1.0.5
     */
    String requestId();

    /**
     * 返回请求是否保持连接。
     *
     * @return 如果请求保持连接，返回 true；否则返回 false
     */
    boolean isKeepAlive();

    /**
     * 返回请求是否为WebSocket连接（已升级）。
     *
     * @return 如果请求为WebSocket连接，返回 true；否则返回 false
     */
    boolean isWebsocket();

    /**
     * 返回解析后的请求方法（如 HTTP 1.1 等）。
     *
     * @return 解析后的请求方法（如 HTTP 1.1 等）
     */
    HttpMethod method();

    /**
     * 返回从 {@link #uri()} 解码后的路径部分，去掉前后的 '/'。
     *
     * @return 从 {@link #uri()} 解码后的路径部分，去掉前后的 '/'
     */
    default String path() {
        String path = fullPath();
        if (!path.isEmpty()) {
            if (path.charAt(0) == '/') {
                path = path.substring(1);
                if (path.isEmpty()) {
                    return path;
                }
            }
            if (path.charAt(path.length() - 1) == '/') {
                return path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    /**
     * 返回解析后的目标地址。
     *
     * @return 解析后的目标地址
     */
    String uri();

    /**
     * 返回解析后的请求版本（如 HTTP 1.1 等）。
     *
     * @return 解析后的请求版本（如 HTTP 1.1 等）
     */
    HttpVersion version();
}
