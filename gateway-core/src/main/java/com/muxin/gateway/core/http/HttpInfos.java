package com.muxin.gateway.core.http;


import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * An Http Reactive Channel with several accessors related to HTTP flow: headers, params,
 * URI, method, websocket...
 *
 * @author Stephane Maldini
 * @since 0.5
 */
public interface HttpInfos {

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
     * Returns true if websocket connection (upgraded).
     *
     * @return true if websocket connection
     */
    boolean isWebsocket();

    /**
     * Returns the resolved request method (HTTP 1.1 etc.).
     *
     * @return the resolved request method (HTTP 1.1 etc.)
     */
    HttpMethod method();

    /**
     * Returns the decoded path portion from the {@link #uri()} without the leading and trailing '/' if present.
     *
     * @return the decoded path portion from the {@link #uri()} without the leading and trailing '/' if present
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
     * Returns the resolved target address.
     *
     * @return the resolved target address
     */
    String uri();

    /**
     * Returns the resolved request version (HTTP 1.1 etc).
     *
     * @return the resolved request version (HTTP 1.1 etc)
     */
    HttpVersion version();

}
