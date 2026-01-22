package com.muxin.gateway.core.plus.message.http;

import io.netty.handler.codec.http.HttpMethod;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * HTTP请求消息接口
 * 定义HTTP请求消息的接口，包含请求方法、URI等请求特定属性
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpRequestMessage extends HttpMessage {

    HttpMethod method();

    void setMethod(HttpMethod httpMethod);

    String uri();

    /**
     * Returns the decoded path portion from the {@link #uri()}.
     *
     * @return the decoded path portion from the {@link #uri()}
     * @since 0.9.6
     */
    String fullPath();

    /**
     * Return a unique id for the request. The id is a combination
     * of the id of the underlying connection and the serial number of the request
     * received on that connection.
     * <p>Format of the id:
     * {@code <CONNECTION_ID>-<REQUEST_NUMBER>}
     * </p>
     * <p>
     * Example:
     * {@code
     *     <CONNECTION_ID>: 329c6ffd
     *     <REQUEST_NUMBER>: 5
     *
     *     Result: 329c6ffd-5
     * }
     * </p>
     *
     * @return an unique id for the request
     * @since 1.0.5
     */
    String requestId();

    /**
     * Is the request keep alive.
     *
     * @return is keep alive
     */
    boolean isKeepAlive();

    /**
     * URI parameter captured via {@code {}} e.g. {@code /test/{param}}.
     *
     * @param key parameter name e.g. {@code "param"} in URI {@code /test/{param}}
     * @return the parameter captured value
     */

    String param(CharSequence key);

    /**
     * Returns all URI parameters captured via {@code {}} e.g. {@code /test/{param1}/{param2}} as key/value map.
     *
     * @return the parameters captured key/value map
     */

    Map<String, String> params();



    ZonedDateTime timestamp();

}
