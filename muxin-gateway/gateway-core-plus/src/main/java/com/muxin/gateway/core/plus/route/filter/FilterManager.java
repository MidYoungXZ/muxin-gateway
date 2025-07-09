package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;
import com.muxin.gateway.core.plus.message.Protocol;

/**
 * @author Administrator
 * @since 1.0
 */
public interface FilterManager extends Repository<String, Filter>, LifeCycle {

    /**
     * 创建过滤器链
     */
    FilterChain createFilterChain(Protocol protocol);

    /**
     * 创建指定类型的过滤器链
     */
    FilterChain createFilterChain(Protocol protocol, FilterType type);

}
