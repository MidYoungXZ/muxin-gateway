package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;

/**
 * HTTP过滤器接口
 * 简化版本：只支持HTTP协议
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface Filter {

    void filter(HttpServerExchange exchange, FilterChain chain);

    String getName();

    FilterType getType();

    int getOrder();

    boolean isEnabled();
}
