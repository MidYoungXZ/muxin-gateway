package com.muxin.gateway.core.http;

import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

/**
 * 定义了一个HTTP服务器信息接口，继承自 {@link HttpInfos} 和 {@link ConnectionInformation}。
 * 该接口提供了访问HTTP服务器相关信息的方法，包括HTTP头信息、参数、URI、方法、WebSocket等，以及连接信息和Cookie。
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface HttpServerInfos extends HttpInfos, ConnectionInformation {

    /**
     * 返回解析后的HTTP Cookie。与 {@link #cookies()} 不同，此方法返回所有Cookie，即使它们具有相同的名称。
     *
     * @return 解析后的HTTP Cookie映射
     */
    Map<CharSequence, List<Cookie>> allCookies();
}
