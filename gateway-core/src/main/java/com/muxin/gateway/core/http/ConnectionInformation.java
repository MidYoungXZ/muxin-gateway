package com.muxin.gateway.core.http;

import java.net.SocketAddress;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface ConnectionInformation {

    /**
     * Returns the address of the host which received the request, possibly {@code null} in case of Unix Domain Sockets.
     * The returned address is the merged information from all proxies.
     *
     * @return the address merged from all proxies of the host which received the request
     */

    SocketAddress hostAddress();

    /**
     * Returns the address of the host which received the request, possibly {@code null} in case of Unix Domain Sockets.
     *
     * @return the address of the host which received the request
     */

    SocketAddress connectionHostAddress();

    /**
     * Returns the address of the client that initiated the request, possibly {@code null} in case of Unix Domain Sockets.
     * The returned address is the merged information from all proxies.
     *
     * @return the address merged from all proxies of the client that initiated the request
     */

    SocketAddress remoteAddress();

    /**
     * Returns the address of the client that initiated the request, possibly {@code null} in case of Unix Domain Sockets.
     *
     * @return the address of the client that initiated the request
     */

    SocketAddress connectionRemoteAddress();

    /**
     * Returns the current protocol scheme.
     * The returned address is the merged information from all proxies.
     *
     * @return the protocol scheme merged from all proxies
     */
    String scheme();

    /**
     * Returns the current protocol scheme.
     *
     * @return the protocol scheme
     */
    String connectionScheme();

    /**
     * Returns the host name derived from the {@code Host}/{@code X-Forwarded-Host}/{@code Forwarded} header
     * associated with this request.
     *
     * @return the host name derived from the {@code Host}/{@code X-Forwarded-Host}/{@code Forwarded} header
     * associated with this request.
     * @since 1.0.29
     */
    String hostName();

    /**
     * Returns the host port derived from the {@code Host}/{@code X-Forwarded-*}/{@code Forwarded} header
     * associated with this request.
     *
     * @return the host port derived from the {@code Host}/{@code X-Forwarded-*}/{@code Forwarded} header
     * associated with this request.
     * @since 1.0.29
     */
    int hostPort();
}