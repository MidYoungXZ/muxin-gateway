package com.muxin.gateway.refactory.filter;

import com.muxin.gateway.refactory.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 通用过滤器管理器实现
 *
 * @author muxin
 */
@Slf4j
public class UniversalFilterManager implements FilterManager {

    private final Map<String, UniversalFilter> filters = new HashMap<>();

    @Override
    public UniversalFilter save(UniversalFilter filter) {
        filters.put(filter.getName(), filter);
        log.info("注册过滤器: {}", filter.getName());
        return filter;
    }

    @Override
    public void removeByUniqueCode(String filterName) {
        filters.remove(filterName);
    }

    @Override
    public UniversalFilter findByUniqueCode(String filterName) {
        return filters.get(filterName);
    }

    @Override
    public Collection<UniversalFilter> findAll() {
        return filters.values();
    }



    @Override
    public UniversalFilterChain createFilterChain(Protocol protocol) {
        return null;
    }

    @Override
    public UniversalFilterChain createFilterChain(Protocol protocol, FilterType type) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}