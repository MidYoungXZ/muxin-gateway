package com.muxin.gateway.refactory.filter;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.Protocol;

/**
 * @author Administrator
 * @since 1.0
 */
public interface FilterManager extends Repository<String, UniversalFilter> {

    /**
     * 创建过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol);

    /**
     * 创建指定类型的过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol, FilterType type);

}
