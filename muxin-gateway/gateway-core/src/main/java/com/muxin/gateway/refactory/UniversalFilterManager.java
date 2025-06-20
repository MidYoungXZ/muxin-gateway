package com.muxin.gateway.refactory;

import java.util.*;

/**
 * 通用过滤器管理器实现
 *
 * @author muxin
 */
public class UniversalFilterManager implements FilterManager {
    
    private final Map<String, UniversalFilter> filters = new HashMap<>();
    
    @Override
    public void registerFilter(UniversalFilter filter) {
        filters.put(filter.getName(), filter);
        System.out.println("Registered filter: " + filter.getName());
    }
    
    @Override
    public void unregisterFilter(String filterName) {
        filters.remove(filterName);
    }
    
    @Override
    public List<UniversalFilter> getFilters(FilterType type) {
        List<UniversalFilter> result = new ArrayList<>();
        for (UniversalFilter filter : filters.values()) {
            if (filter.getType() == type) {
                result.add(filter);
            }
        }
        return result;
    }
    
    @Override
    public List<UniversalFilter> getFilters(Protocol protocol, FilterType type) {
        List<UniversalFilter> result = new ArrayList<>();
        for (UniversalFilter filter : filters.values()) {
            if (filter.getType() == type && filter.getSupportedProtocols().contains(protocol)) {
                result.add(filter);
            }
        }
        return result;
    }
    
    @Override
    public UniversalFilterChain createFilterChain(Protocol protocol) {
        List<UniversalFilter> allFilters = new ArrayList<>();
        for (UniversalFilter filter : filters.values()) {
            if (filter.getSupportedProtocols().contains(protocol)) {
                allFilters.add(filter);
            }
        }
        return new SimpleFilterChain(allFilters);
    }
    
    @Override
    public UniversalFilterChain createFilterChain(Protocol protocol, FilterType type) {
        return new SimpleFilterChain(getFilters(protocol, type));
    }
    
    @Override
    public void enableFilter(String filterName) {
        System.out.println("Enabled filter: " + filterName);
    }
    
    @Override
    public void disableFilter(String filterName) {
        System.out.println("Disabled filter: " + filterName);
    }
    
    @Override
    public UniversalFilter getFilter(String filterName) {
        return filters.get(filterName);
    }
    
    @Override
    public List<UniversalFilter> getAllFilters() {
        return new ArrayList<>(filters.values());
    }
} 