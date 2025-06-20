package com.muxin.gateway.refactory;

import java.util.List;

/**
 * 过滤器管理器接口
 * 负责过滤器的注册、管理和执行
 *
 * @author muxin
 */
public interface FilterManager {
    
    /**
     * 注册过滤器
     */
    void registerFilter(UniversalFilter filter);
    
    /**
     * 注销过滤器
     */
    void unregisterFilter(String filterName);
    
    /**
     * 获取指定类型的过滤器
     */
    List<UniversalFilter> getFilters(FilterType type);
    
    /**
     * 获取支持指定协议的过滤器
     */
    List<UniversalFilter> getFilters(Protocol protocol, FilterType type);
    
    /**
     * 创建过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol);
    
    /**
     * 创建指定类型的过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol, FilterType type);
    
    /**
     * 启用过滤器
     */
    void enableFilter(String filterName);
    
    /**
     * 禁用过滤器
     */
    void disableFilter(String filterName);
    
    /**
     * 获取过滤器
     */
    UniversalFilter getFilter(String filterName);
    
    /**
     * 获取所有过滤器
     */
    List<UniversalFilter> getAllFilters();
} 