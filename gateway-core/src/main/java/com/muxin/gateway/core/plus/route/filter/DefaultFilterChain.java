package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 默认过滤器链实现
 * 责任链模式：按顺序执行过滤器，Filter 通过调用 chain.doFilter() 触发下一个
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultFilterChain implements FilterChain {

    private final List<Filter> filters;
    private int currentIndex = 0;

    public DefaultFilterChain(List<Filter> filters) {
        this.filters = filters != null ? filters : Collections.emptyList();
    }

    @Override
    public void doFilter(HttpServerExchange exchange) {
        if (currentIndex < filters.size()) {
            Filter filter = filters.get(currentIndex++);
            if (log.isDebugEnabled()) {
                log.debug("[DefaultFilterChain] 执行过滤器: {} (索引: {}/{})",
                        filter.getName(), currentIndex, filters.size());
            }
            filter.filter(exchange, this);
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex < filters.size();
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public int getTotalCount() {
        return filters.size();
    }

    /**
     * 重置过滤器链（用于重用）
     */
    public void reset() {
        currentIndex = 0;
    }
}