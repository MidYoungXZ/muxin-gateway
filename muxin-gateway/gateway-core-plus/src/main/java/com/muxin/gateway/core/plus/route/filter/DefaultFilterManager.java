package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用过滤器管理器实现
 *
 * @author muxin
 */
@Slf4j
public class DefaultFilterManager implements FilterManager {

    private final Map<String, Filter> filters = new HashMap<>();

    @Override
    public Filter save(Filter filter) {
        filters.put(filter.getName(), filter);
        log.info("注册过滤器: {}", filter.getName());
        return filter;
    }

    @Override
    public void removeByUniqueCode(String filterName) {
        filters.remove(filterName);
    }

    @Override
    public Filter findByUniqueCode(String filterName) {
        return filters.get(filterName);
    }

    @Override
    public Collection<Filter> findAll() {
        return filters.values();
    }



    @Override
    public FilterChain createFilterChain(Protocol protocol) {
        // 获取所有适用于该协议的过滤器
        List<Filter> allFilters = filters.values().stream()
                .filter(filter -> filter.isEnabled())
                .filter(filter -> filter.getSupportedProtocols().contains(protocol) || filter.getSupportedProtocols().isEmpty())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .collect(java.util.stream.Collectors.toList());
        
        log.debug("为协议 {} 创建过滤器链，包含 {} 个过滤器", protocol.getName(), allFilters.size());
        return new DefaultFilterChain(allFilters);
    }

    @Override
    public FilterChain createFilterChain(Protocol protocol, FilterType type) {
        // 获取指定类型和协议的过滤器
        List<Filter> typeFilters = filters.values().stream()
                .filter(filter -> filter.isEnabled())
                .filter(filter -> filter.getSupportedProtocols().contains(protocol) || filter.getSupportedProtocols().isEmpty())
                .filter(filter -> filter.getType() == type)
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .collect(Collectors.toList());
        
        log.debug("为协议 {} 和类型 {} 创建过滤器链，包含 {} 个过滤器", 
                protocol.getName(), type, typeFilters.size());
        return new DefaultFilterChain(typeFilters);
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