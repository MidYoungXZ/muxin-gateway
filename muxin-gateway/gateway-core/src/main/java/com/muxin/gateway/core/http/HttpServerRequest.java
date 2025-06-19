package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface HttpServerRequest extends HttpServerInfos {


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

    /**
     * Specifies a params resolver.
     *
     * @param paramsResolver a params resolver
     * @return this {@link HttpServerRequest}
     */
    HttpServerRequest paramsResolver(Function<? super String, Map<String, String>> paramsResolver);


    /**
     * Returns true if the request has {@code Content-Type} with value {@code application/x-www-form-urlencoded}.
     *
     * @return true if the request has {@code Content-Type} with value {@code application/x-www-form-urlencoded},
     * false - otherwise
     * @since 1.0.11
     */
    boolean isFormUrlencoded();

    /**
     * Returns true if the request has {@code Content-Type} with value {@code multipart/form-data}.
     *
     * @return true if the request has {@code Content-Type} with value {@code multipart/form-data},
     * false - otherwise
     * @since 1.0.11
     */
    boolean isMultipart();


    /**
     * Returns inbound {@link HttpHeaders}.
     *
     * @return inbound {@link HttpHeaders}
     */
    HttpHeaders requestHeaders();

    /**
     * Returns the time when the request was received.
     *
     * @return the time when the request was received
     * @since 1.0.28
     */
    ZonedDateTime timestamp();


    ByteBuf body();
}