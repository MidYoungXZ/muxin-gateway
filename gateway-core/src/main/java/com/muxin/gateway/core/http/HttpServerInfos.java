package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface HttpServerInfos extends HttpInfos, ConnectionInformation {

    /**
     * Returns resolved HTTP cookies. As opposed to {@link #()}, this
     * returns all cookies, even if they have the same name.
     *
     * @return Resolved HTTP cookies
     */
    Map<CharSequence, List<Cookie>> allCookies();

}
