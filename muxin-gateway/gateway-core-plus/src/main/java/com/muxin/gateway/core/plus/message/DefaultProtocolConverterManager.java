package com.muxin.gateway.core.plus.message;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 默认协议转换管理器实现
 * 支持协议转换链和性能统计
 * 
 * @author muxin
 * @since 2.0
 */
@Slf4j
public class DefaultProtocolConverterManager implements ProtocolConverterManager {

    // 转换器存储：key = "sourceProtocol:targetProtocol"
    private final Map<String, ProtocolConverter> converters = new ConcurrentHashMap<>();
    
    // 转换链缓存：key = "sourceProtocol:targetProtocol"
    private final Map<String, List<ProtocolConverter>> conversionChainCache = new ConcurrentHashMap<>();
    
    // 统计信息
    private final AtomicLong totalConversionRequests = new AtomicLong(0);
    private final AtomicLong successfulConversions = new AtomicLong(0);
    private final AtomicLong failedConversions = new AtomicLong(0);
    private final AtomicLong totalConversionTime = new AtomicLong(0);
    private final Map<String, AtomicLong> conversionUsageCount = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    private final long startTime;

    public DefaultProtocolConverterManager() {
        this.startTime = System.currentTimeMillis();
        log.info("[DefaultProtocolConverterManager] 协议转换管理器创建完成");
    }

    @Override
    public ProtocolConverter getConverter(Protocol sourceProtocol, Protocol targetProtocol) {
        if (sourceProtocol == null || targetProtocol == null) {
            return null;
        }
        
        String key = buildConverterKey(sourceProtocol, targetProtocol);
        ProtocolConverter converter = converters.get(key);
        
        if (converter != null) {
            // 记录使用统计
            conversionUsageCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            totalConversionRequests.incrementAndGet();
        }
        
        return converter;
    }

    @Override
    public void registerConverter(ProtocolConverter converter) {
        if (converter == null) {
            throw new IllegalArgumentException("转换器不能为空");
        }
        
        Protocol sourceProtocol = converter.getSupportedSourceProtocol();
        Protocol targetProtocol = converter.getSupportedTargetProtocol();
        
        if (sourceProtocol == null || targetProtocol == null) {
            throw new IllegalArgumentException("转换器的源协议和目标协议不能为空");
        }
        
        String key = buildConverterKey(sourceProtocol, targetProtocol);
        
        if (converters.containsKey(key)) {
            log.warn("[DefaultProtocolConverterManager] 转换器已存在，将被替换: {} -> {}", 
                sourceProtocol.getName(), targetProtocol.getName());
        }
        
        converters.put(key, converter);
        
        // 清除转换链缓存，因为新增了转换器
        conversionChainCache.clear();
        
        log.info("[DefaultProtocolConverterManager] 注册转换器: {} -> {}", 
            sourceProtocol.getName(), targetProtocol.getName());
    }

    @Override
    public ProtocolConverter unregisterConverter(Protocol sourceProtocol, Protocol targetProtocol) {
        if (sourceProtocol == null || targetProtocol == null) {
            return null;
        }
        
        String key = buildConverterKey(sourceProtocol, targetProtocol);
        ProtocolConverter removed = converters.remove(key);
        
        if (removed != null) {
            // 清除转换链缓存
            conversionChainCache.clear();
            // 清除使用统计
            conversionUsageCount.remove(key);
            
            log.info("[DefaultProtocolConverterManager] 注销转换器: {} -> {}", 
                sourceProtocol.getName(), targetProtocol.getName());
        }
        
        return removed;
    }

    @Override
    public boolean canConvert(Protocol sourceProtocol, Protocol targetProtocol) {
        return getConverter(sourceProtocol, targetProtocol) != null || 
               !getConversionChain(sourceProtocol, targetProtocol).isEmpty();
    }

    @Override
    public List<ProtocolConverter> getConversionChain(Protocol sourceProtocol, Protocol targetProtocol) {
        if (sourceProtocol == null || targetProtocol == null) {
            return Collections.emptyList();
        }
        
        // 如果源协议和目标协议相同，返回空链
        if (sourceProtocol.equals(targetProtocol)) {
            return Collections.emptyList();
        }
        
        String cacheKey = buildConverterKey(sourceProtocol, targetProtocol);
        
        // 先检查缓存
        List<ProtocolConverter> cachedChain = conversionChainCache.get(cacheKey);
        if (cachedChain != null) {
            return cachedChain;
        }
        
        // 构建转换链
        List<ProtocolConverter> chain = buildConversionChain(sourceProtocol, targetProtocol);
        
        // 缓存结果
        conversionChainCache.put(cacheKey, chain);
        
        return chain;
    }

    @Override
    public Set<Protocol> getSupportedSourceProtocols() {
        return converters.values().stream()
            .map(ProtocolConverter::getSupportedSourceProtocol)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Protocol> getSupportedTargetProtocols() {
        return converters.values().stream()
            .map(ProtocolConverter::getSupportedTargetProtocol)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Protocol> getConvertibleTargets(Protocol sourceProtocol) {
        if (sourceProtocol == null) {
            return Collections.emptySet();
        }
        
        return converters.values().stream()
            .filter(converter -> sourceProtocol.equals(converter.getSupportedSourceProtocol()))
            .map(ProtocolConverter::getSupportedTargetProtocol)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Protocol> getConvertibleSources(Protocol targetProtocol) {
        if (targetProtocol == null) {
            return Collections.emptySet();
        }
        
        return converters.values().stream()
            .filter(converter -> targetProtocol.equals(converter.getSupportedTargetProtocol()))
            .map(ProtocolConverter::getSupportedSourceProtocol)
            .collect(Collectors.toSet());
    }

    @Override
    public List<ProtocolConverter> getAllConverters() {
        return new ArrayList<>(converters.values());
    }

    @Override
    public ConverterManagerStats getStats() {
        return new DefaultConverterManagerStats();
    }

    @Override
    public void clearAll() {
        converters.clear();
        conversionChainCache.clear();
        conversionUsageCount.clear();
        
        log.info("[DefaultProtocolConverterManager] 清除所有转换器");
    }

    @Override
    public void init() {
        log.info("[DefaultProtocolConverterManager] 初始化协议转换管理器");
    }

    @Override
    public void start() {
        running = true;
        log.info("[DefaultProtocolConverterManager] 启动协议转换管理器");
    }

    @Override
    public void shutdown() {
        running = false;
        clearAll();
        log.info("[DefaultProtocolConverterManager] 关闭协议转换管理器");
    }

    /**
     * 检查管理器是否运行中
     * 
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 构建转换器键
     */
    private String buildConverterKey(Protocol sourceProtocol, Protocol targetProtocol) {
        return sourceProtocol.getName() + ":" + sourceProtocol.getVersion() + 
               " -> " + 
               targetProtocol.getName() + ":" + targetProtocol.getVersion();
    }

    /**
     * 构建转换链（使用BFS算法寻找最短转换路径）
     * 支持多跳转换，找到从源协议到目标协议的最短路径
     */
    private List<ProtocolConverter> buildConversionChain(Protocol sourceProtocol, Protocol targetProtocol) {
        // 1. 直接转换
        ProtocolConverter directConverter = converters.get(buildConverterKey(sourceProtocol, targetProtocol));
        if (directConverter != null) {
            return Collections.singletonList(directConverter);
        }
        
        // 2. 使用BFS算法寻找最短转换路径
        return findShortestConversionPath(sourceProtocol, targetProtocol);
    }
    
    /**
     * 使用广度优先搜索（BFS）算法寻找最短转换路径
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 转换器链，如果找不到路径则返回空列表
     */
    private List<ProtocolConverter> findShortestConversionPath(Protocol sourceProtocol, Protocol targetProtocol) {
        // BFS用的队列：存储当前协议和到达该协议的转换路径
        Queue<ConversionPath> queue = new LinkedList<>();
        // 已访问的协议，避免循环
        Set<Protocol> visited = new HashSet<>();
        
        // 初始化：从源协议开始
        queue.offer(new ConversionPath(sourceProtocol, new ArrayList<>()));
        visited.add(sourceProtocol);
        
        while (!queue.isEmpty()) {
            ConversionPath currentPath = queue.poll();
            Protocol currentProtocol = currentPath.protocol;
            
            // 找到所有从当前协议出发的转换器
            for (ProtocolConverter converter : converters.values()) {
                if (!converter.getSupportedSourceProtocol().equals(currentProtocol)) {
                    continue; // 源协议不匹配，跳过
                }
                
                Protocol nextProtocol = converter.getSupportedTargetProtocol();
                
                // 避免循环路径
                if (visited.contains(nextProtocol)) {
                    continue;
                }
                
                // 构建新的路径
                List<ProtocolConverter> newConverterChain = new ArrayList<>(currentPath.converterChain);
                newConverterChain.add(converter);
                
                // 检查是否到达目标协议
                if (nextProtocol.equals(targetProtocol)) {
                    log.debug("[DefaultProtocolConverterManager] 找到转换链: {} -> {} (长度: {})", 
                        sourceProtocol.getName(), targetProtocol.getName(), newConverterChain.size());
                    return newConverterChain;
                }
                
                // 添加到队列继续搜索（避免路径过长）
                if (newConverterChain.size() < 5) { // 限制最大跳数，避免无限搜索
                    queue.offer(new ConversionPath(nextProtocol, newConverterChain));
                    visited.add(nextProtocol);
                }
            }
        }
        
        // 未找到转换路径
        log.debug("[DefaultProtocolConverterManager] 未找到转换链: {} -> {}", 
            sourceProtocol.getName(), targetProtocol.getName());
        return Collections.emptyList();
    }
    
    /**
     * 内部类：表示转换路径
     */
    private static class ConversionPath {
        final Protocol protocol;
        final List<ProtocolConverter> converterChain;
        
        ConversionPath(Protocol protocol, List<ProtocolConverter> converterChain) {
            this.protocol = protocol;
            this.converterChain = converterChain;
        }
    }

    /**
     * 记录转换成功
     */
    public void recordConversionSuccess(long duration) {
        successfulConversions.incrementAndGet();
        totalConversionTime.addAndGet(duration);
    }

    /**
     * 记录转换失败
     */
    public void recordConversionFailure() {
        failedConversions.incrementAndGet();
    }

    /**
     * 默认统计信息实现
     */
    private class DefaultConverterManagerStats implements ConverterManagerStats {

        @Override
        public int getRegisteredConvertersCount() {
            return converters.size();
        }

        @Override
        public int getSupportedConversionsCount() {
            return converters.size();
        }

        @Override
        public long getTotalConversionRequests() {
            return totalConversionRequests.get();
        }

        @Override
        public long getSuccessfulConversions() {
            return successfulConversions.get();
        }

        @Override
        public long getFailedConversions() {
            return failedConversions.get();
        }

        @Override
        public double getAverageConversionTime() {
            long successful = getSuccessfulConversions();
            return successful > 0 ? (double) totalConversionTime.get() / successful : 0.0;
        }

        @Override
        public Optional<String> getMostUsedConversion() {
            return conversionUsageCount.entrySet().stream()
                .max(Map.Entry.comparingByValue((a1, a2) -> Long.compare(a1.get(), a2.get())))
                .map(Map.Entry::getKey);
        }
    }
} 