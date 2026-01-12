package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

/**
 * HTTP服务器信息接口
 * 
 * 扩展HttpInfos和ConnectionInformation接口，提供HTTP服务器端信息
 *
 * @author Administrator
 * @since 1.0.0
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
