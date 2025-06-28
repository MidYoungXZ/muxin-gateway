package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;
import com.muxin.gateway.core.plus.message.Protocol;

/**
 * @author Administrator
 * @since 1.0
 */
public interface FilterManager extends Repository<String, UniversalFilter>, LifeCycle {

    /**
     * 创建过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol);

    /**
     * 创建指定类型的过滤器链
     */
    UniversalFilterChain createFilterChain(Protocol protocol, FilterType type);

}
