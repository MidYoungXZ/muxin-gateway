package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;

import java.util.List;

/**
 * 过滤器链接口
 * 责任链模式：Filter 执行完成后调用 doFilter() 触发下一个 Filter
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface FilterChain {

    /**
     * 执行下一个过滤器
     * 由当前 Filter 调用，触发链中下一个 Filter 的执行
     *
     * @param exchange HTTP 交换对象
     */
    void doFilter(HttpServerExchange exchange);

    /**
     * 是否还有下一个过滤器
     */
    boolean hasNext();

    /**
     * 获取当前过滤器索引
     */
    int getCurrentIndex();

    /**
     * 获取过滤器总数
     */
    int getTotalCount();

    /**
     * 创建过滤器链
     */
    static FilterChain create(List<Filter> filters) {
        return new DefaultFilterChain(filters);
    }
}