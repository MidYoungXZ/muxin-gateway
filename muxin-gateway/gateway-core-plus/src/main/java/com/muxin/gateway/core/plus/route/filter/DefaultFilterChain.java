package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.route.RequestContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 简单过滤器链实现
 *
 * @author muxin
 */
public class DefaultFilterChain implements FilterChain {
    
    private final List<Filter> filters;
    private int currentIndex;
    
    public DefaultFilterChain() {
        this.filters = new ArrayList<>();
        this.currentIndex = 0;
    }
    
    public DefaultFilterChain(List<Filter> filters) {
        this.filters = new ArrayList<>(filters);
        // 按order排序
        this.filters.sort(Comparator.comparingInt(Filter::getOrder));
        this.currentIndex = 0;
    }
    
    @Override
    public void filter(RequestContext context) {
        if (hasNext()) {
            Filter filter = filters.get(currentIndex++);
            if (filter.isEnabled()) {
                filter.filter(context, this);
            } else {
                // 跳过禁用的过滤器
                filter(context);
            }
        }
        // 如果没有更多过滤器，链执行完毕
    }
    
    @Override
    public boolean hasNext() {
        return currentIndex < filters.size();
    }
    
    @Override
    public void addFilter(Filter filter) {
        filters.add(filter);
        // 重新排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));
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
     * 重置过滤器链，可以重新执行
     */
    public void reset() {
        this.currentIndex = 0;
    }
    
    /**
     * 获取所有过滤器（只读）
     */
    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }
} 