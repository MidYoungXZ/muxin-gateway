package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 默认连接工厂管理器实现
 * 支持工厂注册、事件监听和统计信息
 * 
 * @author muxin
 * @since 2.0
 */
@Slf4j
public class DefaultConnectionFactoryManager implements ConnectionFactoryManager {

    // 工厂存储：key = protocolKey (name:version)
    private final Map<String, ConnectionFactory> factories = new ConcurrentHashMap<>();
    
    // 协议到工厂的映射
    private final Map<Protocol, ConnectionFactory> protocolFactoryMap = new ConcurrentHashMap<>();
    
    // 事件监听器
    private final List<FactoryEventListener> listeners = new CopyOnWriteArrayList<>();
    
    // 统计信息
    private final AtomicLong totalFactoryRequests = new AtomicLong(0);
    private final AtomicLong totalConnectionsCreated = new AtomicLong(0);
    private final AtomicLong totalConnectionCreationFailures = new AtomicLong(0);
    private final AtomicLong totalConnectionCreationTime = new AtomicLong(0);
    private final Map<String, AtomicLong> factoryUsageCount = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    private final long startTime;

    public DefaultConnectionFactoryManager() {
        this.startTime = System.currentTimeMillis();
        log.info("[DefaultConnectionFactoryManager] 连接工厂管理器创建完成");
    }

    @Override
    public ConnectionFactory getFactory(Protocol protocol) {
        if (protocol == null) {
            return null;
        }
        
        String protocolKey = buildProtocolKey(protocol);
        ConnectionFactory factory = factories.get(protocolKey);
        
        if (factory != null) {
            // 记录使用统计
            factoryUsageCount.computeIfAbsent(protocolKey, k -> new AtomicLong(0)).incrementAndGet();
            totalFactoryRequests.incrementAndGet();
            
            log.debug("[DefaultConnectionFactoryManager] 获取连接工厂: {}", protocol.getName());
        } else {
            log.debug("[DefaultConnectionFactoryManager] 未找到协议的连接工厂: {}", protocol.getName());
        }
        
        return factory;
    }

    @Override
    public void registerFactory(ConnectionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("连接工厂不能为空");
        }
        
        Protocol protocol = factory.getSupportedProtocol();
        if (protocol == null) {
            throw new IllegalArgumentException("连接工厂的支持协议不能为空");
        }
        
        String protocolKey = buildProtocolKey(protocol);
        
        // 检查是否已存在
        if (factories.containsKey(protocolKey)) {
            log.warn("[DefaultConnectionFactoryManager] 连接工厂已存在，将被替换: {}", protocol.getName());
            
            // 通知工厂注销事件
            ConnectionFactory oldFactory = factories.get(protocolKey);
            notifyListeners(listener -> listener.onFactoryUnregistered(protocol, oldFactory));
        }
        
        // 注册新工厂
        factories.put(protocolKey, factory);
        protocolFactoryMap.put(protocol, factory);
        
        // 通知工厂注册事件
        notifyListeners(listener -> listener.onFactoryRegistered(protocol, factory));
        
        log.info("[DefaultConnectionFactoryManager] 注册连接工厂: {}", protocol.getName());
    }

    @Override
    public ConnectionFactory unregisterFactory(Protocol protocol) {
        if (protocol == null) {
            return null;
        }
        
        String protocolKey = buildProtocolKey(protocol);
        ConnectionFactory removed = factories.remove(protocolKey);
        
        if (removed != null) {
            protocolFactoryMap.remove(protocol);
            factoryUsageCount.remove(protocolKey);
            
            // 通知工厂注销事件
            notifyListeners(listener -> listener.onFactoryUnregistered(protocol, removed));
            
            log.info("[DefaultConnectionFactoryManager] 注销连接工厂: {}", protocol.getName());
        }
        
        return removed;
    }

    @Override
    public boolean supports(Protocol protocol) {
        return getFactory(protocol) != null;
    }

    @Override
    public Set<Protocol> getSupportedProtocols() {
        return factories.values().stream()
            .map(ConnectionFactory::getSupportedProtocol)
            .collect(Collectors.toSet());
    }

    @Override
    public List<ConnectionFactory> getAllFactories() {
        return new ArrayList<>(factories.values());
    }

    @Override
    public List<ConnectionFactory> getFactoriesByProtocolName(String protocolName) {
        if (protocolName == null) {
            return Collections.emptyList();
        }
        
        return factories.values().stream()
            .filter(factory -> protocolName.equals(factory.getSupportedProtocol().getName()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean warmupAllFactories() {
        log.info("[DefaultConnectionFactoryManager] 开始预热所有连接工厂");
        boolean allSuccess = true;
        
        for (Map.Entry<Protocol, ConnectionFactory> entry : protocolFactoryMap.entrySet()) {
            Protocol protocol = entry.getKey();
            boolean success = warmupFactory(protocol);
            if (!success) {
                allSuccess = false;
            }
        }
        
        log.info("[DefaultConnectionFactoryManager] 连接工厂预热完成，成功: {}", allSuccess);
        return allSuccess;
    }

    @Override
    public boolean warmupFactory(Protocol protocol) {
        if (protocol == null) {
            return false;
        }
        
        ConnectionFactory factory = getFactory(protocol);
        if (factory == null) {
            log.warn("[DefaultConnectionFactoryManager] 未找到协议的连接工厂: {}", protocol.getName());
            return false;
        }
        
        try {
            // 这里可以添加具体的预热逻辑
            log.debug("[DefaultConnectionFactoryManager] 预热连接工厂: {}", protocol.getName());
            
            // 通知预热完成事件
            notifyListeners(listener -> listener.onFactoryWarmupCompleted(protocol, true));
            return true;
            
        } catch (Exception e) {
            log.error("[DefaultConnectionFactoryManager] 预热连接工厂失败: " + protocol.getName(), e);
            
            // 通知预热失败事件
            notifyListeners(listener -> listener.onFactoryWarmupCompleted(protocol, false));
            notifyListeners(listener -> listener.onFactoryError(protocol, e));
            return false;
        }
    }

    @Override
    public boolean shutdownAllFactories() {
        log.info("[DefaultConnectionFactoryManager] 开始关闭所有连接工厂");
        boolean allSuccess = true;
        
        for (Protocol protocol : new HashSet<>(protocolFactoryMap.keySet())) {
            boolean success = shutdownFactory(protocol);
            if (!success) {
                allSuccess = false;
            }
        }
        
        log.info("[DefaultConnectionFactoryManager] 所有连接工厂关闭完成，成功: {}", allSuccess);
        return allSuccess;
    }

    @Override
    public boolean shutdownFactory(Protocol protocol) {
        if (protocol == null) {
            return false;
        }
        
        ConnectionFactory factory = getFactory(protocol);
        if (factory == null) {
            return true; // 如果不存在，视为成功
        }
        
        try {
            // 这里可以添加具体的关闭逻辑
            log.debug("[DefaultConnectionFactoryManager] 关闭连接工厂: {}", protocol.getName());
            
            // 从管理器中移除
            unregisterFactory(protocol);
            
            // 通知关闭成功事件
            notifyListeners(listener -> listener.onFactoryShutdown(protocol, true));
            return true;
            
        } catch (Exception e) {
            log.error("[DefaultConnectionFactoryManager] 关闭连接工厂失败: " + protocol.getName(), e);
            
            // 通知关闭失败事件
            notifyListeners(listener -> listener.onFactoryShutdown(protocol, false));
            notifyListeners(listener -> listener.onFactoryError(protocol, e));
            return false;
        }
    }

    @Override
    public FactoryManagerStats getStats() {
        return new DefaultFactoryManagerStats();
    }

    @Override
    public void clearAll() {
        log.info("[DefaultConnectionFactoryManager] 清除所有连接工厂");
        
        // 通知所有工厂注销事件
        for (Map.Entry<Protocol, ConnectionFactory> entry : protocolFactoryMap.entrySet()) {
            notifyListeners(listener -> listener.onFactoryUnregistered(entry.getKey(), entry.getValue()));
        }
        
        factories.clear();
        protocolFactoryMap.clear();
        factoryUsageCount.clear();
    }

    @Override
    public Optional<ConnectionFactory.ConnectionHealthStatus> getFactoryHealth(Protocol protocol) {
        ConnectionFactory factory = getFactory(protocol);
        if (factory == null) {
            return Optional.empty();
        }
        
        // 简单的健康检查实现
        try {
            // 这里可以调用工厂的健康检查方法
            return Optional.of(ConnectionFactory.ConnectionHealthStatus.HEALTHY);
        } catch (Exception e) {
            log.error("[DefaultConnectionFactoryManager] 检查工厂健康状态失败: " + protocol.getName(), e);
            return Optional.of(ConnectionFactory.ConnectionHealthStatus.UNHEALTHY);
        }
    }

    @Override
    public java.util.Map<Protocol, ConnectionFactory.ConnectionHealthStatus> getAllFactoryHealth() {
        Map<Protocol, ConnectionFactory.ConnectionHealthStatus> healthMap = new HashMap<>();
        
        for (Protocol protocol : protocolFactoryMap.keySet()) {
            getFactoryHealth(protocol).ifPresent(status -> healthMap.put(protocol, status));
        }
        
        return healthMap;
    }

    @Override
    public boolean reloadFactoryConfig(Protocol protocol) {
        if (protocol == null) {
            return false;
        }
        
        ConnectionFactory factory = getFactory(protocol);
        if (factory == null) {
            log.warn("[DefaultConnectionFactoryManager] 未找到协议的连接工厂: {}", protocol.getName());
            return false;
        }
        
        try {
            // 这里可以添加具体的配置重载逻辑
            log.debug("[DefaultConnectionFactoryManager] 重载连接工厂配置: {}", protocol.getName());
            return true;
            
        } catch (Exception e) {
            log.error("[DefaultConnectionFactoryManager] 重载连接工厂配置失败: " + protocol.getName(), e);
            notifyListeners(listener -> listener.onFactoryError(protocol, e));
            return false;
        }
    }

    @Override
    public boolean reloadAllFactoryConfigs() {
        log.info("[DefaultConnectionFactoryManager] 开始重载所有连接工厂配置");
        boolean allSuccess = true;
        
        for (Protocol protocol : protocolFactoryMap.keySet()) {
            boolean success = reloadFactoryConfig(protocol);
            if (!success) {
                allSuccess = false;
            }
        }
        
        log.info("[DefaultConnectionFactoryManager] 所有连接工厂配置重载完成，成功: {}", allSuccess);
        return allSuccess;
    }

    @Override
    public void setFactoryEventListener(FactoryEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.debug("[DefaultConnectionFactoryManager] 设置事件监听器: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void removeFactoryEventListener() {
        if (!listeners.isEmpty()) {
            listeners.clear();
            log.debug("[DefaultConnectionFactoryManager] 移除所有事件监听器");
        }
    }

    @Override
    public void init() {
        log.info("[DefaultConnectionFactoryManager] 初始化连接工厂管理器");
    }

    @Override
    public void start() {
        running = true;
        log.info("[DefaultConnectionFactoryManager] 启动连接工厂管理器");
    }

    @Override
    public void shutdown() {
        running = false;
        shutdownAllFactories();
        listeners.clear();
        log.info("[DefaultConnectionFactoryManager] 关闭连接工厂管理器");
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
     * 添加事件监听器（额外方法）
     */
    public void addListener(FactoryEventListener listener) {
        setFactoryEventListener(listener);
    }

    /**
     * 移除指定事件监听器（额外方法）
     */
    public void removeListener(FactoryEventListener listener) {
        if (listeners.remove(listener)) {
            log.debug("[DefaultConnectionFactoryManager] 移除事件监听器: {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * 构建协议键
     */
    private String buildProtocolKey(Protocol protocol) {
        return protocol.getName() + ":" + protocol.getVersion();
    }

    /**
     * 通知所有监听器
     */
    private void notifyListeners(java.util.function.Consumer<FactoryEventListener> action) {
        for (FactoryEventListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("[DefaultConnectionFactoryManager] 通知监听器发生错误: " + listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 记录连接创建成功
     */
    public void recordConnectionCreated(long duration) {
        totalConnectionsCreated.incrementAndGet();
        totalConnectionCreationTime.addAndGet(duration);
    }

    /**
     * 记录连接创建失败
     */
    public void recordConnectionCreationFailure() {
        totalConnectionCreationFailures.incrementAndGet();
    }

    /**
     * 默认统计信息实现
     */
    private class DefaultFactoryManagerStats implements FactoryManagerStats {

        @Override
        public int getRegisteredFactoriesCount() {
            return factories.size();
        }

        @Override
        public int getSupportedProtocolsCount() {
            return getSupportedProtocols().size();
        }

        @Override
        public int getActiveFactoriesCount() {
            // 简单实现：假设已注册的工厂都是活跃的
            return getRegisteredFactoriesCount();
        }

        @Override
        public long getTotalConnectionsCreated() {
            return totalConnectionsCreated.get();
        }

        @Override
        public long getTotalConnectionCreationFailures() {
            return totalConnectionCreationFailures.get();
        }

        @Override
        public double getAverageConnectionCreationTime() {
            long created = getTotalConnectionsCreated();
            return created > 0 ? (double) totalConnectionCreationTime.get() / created : 0.0;
        }

        @Override
        public Optional<Protocol> getMostUsedProtocol() {
            return factoryUsageCount.entrySet().stream()
                .max(Map.Entry.comparingByValue((a1, a2) -> Long.compare(a1.get(), a2.get())))
                .map(Map.Entry::getKey)
                .map(protocolKey -> {
                    // 根据协议键找到对应的协议对象
                    return protocolFactoryMap.keySet().stream()
                        .filter(protocol -> buildProtocolKey(protocol).equals(protocolKey))
                        .findFirst()
                        .orElse(null);
                });
        }

        @Override
        public long getManagerStartupTime() {
            return startTime;
        }
    }
} 