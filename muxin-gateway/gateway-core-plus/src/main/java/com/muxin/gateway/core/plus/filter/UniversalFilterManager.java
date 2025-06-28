package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.monitor.MonitorMetadata;
import com.muxin.gateway.core.plus.monitor.Monitorable;
import com.muxin.gateway.core.plus.monitor.MonitorType;
import com.muxin.gateway.core.plus.monitor.MetricsRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 通用过滤器管理器实现
 *
 * @author muxin
 */
@Slf4j
public class UniversalFilterManager implements FilterManager, Monitorable {

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
        return new SimpleFilterChain(allFilters);
    }

    @Override
    public FilterChain createFilterChain(Protocol protocol, FilterType type) {
        // 获取指定类型和协议的过滤器
        List<Filter> typeFilters = filters.values().stream()
                .filter(filter -> filter.isEnabled())
                .filter(filter -> filter.getSupportedProtocols().contains(protocol) || filter.getSupportedProtocols().isEmpty())
                .filter(filter -> filter.getType() == type)
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .collect(java.util.stream.Collectors.toList());
        
        log.debug("为协议 {} 和类型 {} 创建过滤器链，包含 {} 个过滤器", 
                protocol.getName(), type, typeFilters.size());
        return new SimpleFilterChain(typeFilters);
    }

    // ========== Monitorable 接口实现 ==========

    @Override
    public String getMonitorId() {
        return "filter-manager";
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.FILTER_MANAGER;
    }

    @Override
    public void registerMetrics(MetricsRegistry registry) {
        // 注册过滤器相关指标
        log.info("[UniversalFilterManager] 监控指标注册完成: {}", getMonitorId());
    }

    @Override
    public MonitorMetadata getMonitorMetadata() {
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