package com.muxin.gateway.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

/**
 * An Http Reactive Channel with several accessors related to HTTP flow: headers, params,
 * URI, method, websocket...
 *
 * @author Stephane Maldini
 * @since 0.5
 */
public interface HttpServerResponse extends HttpServerInfos {

    /**
     * Adds an outbound cookie.
     *
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse addCookie(Cookie cookie);

    /**
     * Adds an outbound HTTP header, appending the value if the header already exist.
     *
     * @param name  header name
     * @param value header value
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse addHeader(CharSequence name, CharSequence value);

    /**
     * Sets Transfer-Encoding header.
     *
     * @param chunked true if Transfer-Encoding: chunked
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse chunkedTransfer(boolean chunked);

    /**
     * Enables/Disables compression handling (gzip/deflate) for the underlying response.
     *
     * @param compress should handle compression
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse compression(boolean compress);

    /**
     * Sets an outbound HTTP header, replacing any pre-existing value.
     *
     * @param name  headers key
     * @param value header value
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse header(CharSequence name, CharSequence value);

    /**
     * Sets outbound HTTP headers, replacing any pre-existing value for these headers.
     *
     * @param headers netty headers map
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse headers(Map<String,String> headers);

    /**
     * Sets the request {@code keepAlive} if true otherwise remove the existing connection keep alive header.
     *
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse keepAlive(boolean keepAlive);

    /**
     * Sets an HTTP status to be sent along with the headers.
     *
     * @param status an HTTP status to be sent along with the headers
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse status(HttpResponseStatus status);

    /**
     * Sets an HTTP status to be sent along with the headers.
     *
     * @param status an HTTP status to be sent along with the headers
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse status(int status);

    /**
     * Sets response body with string content
     * 
     * @param body string body
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse body(String body);

    /**
     * Sets response body with byte array content
     * 
     * @param body byte array body
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse body(byte[] body);

    /**
     * Sets response body with ByteBuf content
     * 
     * @param body ByteBuf body
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse body(ByteBuf body);

    /**
     * Gets header value
     * 
     * @param name header name
     * @return header value
     */
    String getHeader(CharSequence name);

    /**
     * Checks if header exists
     * 
     * @param name header name
     * @return true if exists
     */
    boolean hasHeader(CharSequence name);

    /**
     * Returns the outbound HTTP headers, sent back to the clients.
     *
     * @return headers sent back to the clients
     */
    HttpHeaders responseHeaders();

    /**
     * Returns the assigned HTTP status.
     *
     * @return the assigned HTTP status
     */
    HttpResponseStatus status();

    /**
     * Get all cookies
     * 
     * @return list of cookies
     */
    List<Cookie> getCookies();

    /**
     * Get response content
     * 
     * @return ByteBuf content
     */
    ByteBuf content();

}
