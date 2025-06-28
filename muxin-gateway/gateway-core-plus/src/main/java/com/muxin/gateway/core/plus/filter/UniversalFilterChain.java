package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.route.UniversalRequestContext;

/**
 * 通用过滤器链接口
 *
 * @author muxin
 */
public interface UniversalFilterChain {
    
    /**
     * 执行下一个过滤器
     */
    void filter(UniversalRequestContext context);
    
    /**
     * 是否有下一个过滤器
     */
    boolean hasNext();
    
    /**
     * 添加过滤器
     */
    void addFilter(UniversalFilter filter);
    
    /**
     * 获取当前过滤器索引
     */
    int getCurrentIndex();
    
    /**
     * 获取总过滤器数量
     */
    int getTotalCount();
} 