package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;

/**
 * 过滤器链接口
 * 简化版本：只支持HTTP协议
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface FilterChain {

    void filter(HttpServerExchange exchange, FilterChain chain);

    boolean hasNext();

    void addFilter(Filter filter);

    int getCurrentIndex();

    int getTotalCount();

    default void doFilter(HttpServerExchange exchange) {
    }
}
