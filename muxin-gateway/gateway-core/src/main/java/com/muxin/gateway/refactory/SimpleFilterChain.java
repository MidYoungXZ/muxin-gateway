package com.muxin.gateway.refactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 简单过滤器链实现
 *
 * @author muxin
 */
public class SimpleFilterChain implements UniversalFilterChain {
    
    private final List<UniversalFilter> filters;
    private int currentIndex;
    
    public SimpleFilterChain() {
        this.filters = new ArrayList<>();
        this.currentIndex = 0;
    }
    
    public SimpleFilterChain(List<UniversalFilter> filters) {
        this.filters = new ArrayList<>(filters);
        // 按order排序
        this.filters.sort(Comparator.comparingInt(UniversalFilter::getOrder));
        this.currentIndex = 0;
    }
    
    @Override
    public void filter(UniversalRequestContext context) {
        if (hasNext()) {
            UniversalFilter filter = filters.get(currentIndex++);
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
    public void addFilter(UniversalFilter filter) {
        filters.add(filter);
        // 重新排序
        filters.sort(Comparator.comparingInt(UniversalFilter::getOrder));
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
    public List<UniversalFilter> getFilters() {
        return new ArrayList<>(filters);
    }
} 