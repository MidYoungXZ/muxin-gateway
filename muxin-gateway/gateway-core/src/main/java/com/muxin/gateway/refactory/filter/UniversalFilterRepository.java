package com.muxin.gateway.refactory.filter;

import com.muxin.gateway.refactory.Protocol;

import java.util.*;
import java.util.function.Predicate;

/**
 * 通用过滤器管理器实现
 *
 * @author muxin
 */
public class UniversalFilterRepository implements FilterManager {

    private final Map<String, UniversalFilter> filters = new HashMap<>();

    @Override
    public UniversalFilter save(UniversalFilter filter) {
        filters.put(filter.getName(), filter);
        System.out.println("Registered filter: " + filter.getName());
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
}