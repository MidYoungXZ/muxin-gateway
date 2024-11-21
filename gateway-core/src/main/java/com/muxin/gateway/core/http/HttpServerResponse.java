package com.muxin.gateway.core.http;


import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.function.Consumer;

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
    HttpServerResponse headers(HttpHeaders headers);

    /**
     * Sets the request {@code keepAlive} if true otherwise remove the existing connection keep alive header.
     *
     * @return this {@link HttpServerResponse}
     */
    HttpServerResponse keepAlive(boolean keepAlive);

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
    default HttpServerResponse status(int status) {
        return status(HttpResponseStatus.valueOf(status));
    }

    /**
     * Callback for setting outbound trailer headers.
     * The callback is invoked when the response is about to be completed.
     * <p><strong>Note:</strong>Only headers names declared with {@link HttpHeaderNames#TRAILER} are accepted.
     * <p><strong>Note:</strong>Trailer headers are sent only when a message body is encoded with the chunked transfer coding
     * <p><strong>Note:</strong>The headers below cannot be sent as trailer headers:
     * <ul>
     *     <li>Age</li>
     *     <li>Cache-Control</li>
     *     <li>Content-Encoding</li>
     *     <li>Content-Length</li>
     *     <li>Content-Range</li>
     *     <li>Content-Type</li>
     *     <li>Date</li>
     *     <li>Expires</li>
     *     <li>Location</li>
     *     <li>Retry-After</li>
     *     <li>Trailer</li>
     *     <li>Transfer-Encoding</li>
     *     <li>Vary</li>
     *     <li>Warning</li>
     * </ul>
     *
     * @param trailerHeaders netty headers map
     * @return this {@link HttpServerResponse}
     * @since 1.0.12
     */
    HttpServerResponse trailerHeaders(Consumer<? super HttpHeaders> trailerHeaders);
}
