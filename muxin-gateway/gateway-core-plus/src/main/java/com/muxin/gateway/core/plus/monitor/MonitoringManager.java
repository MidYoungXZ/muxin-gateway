package com.muxin.gateway.core.plus.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 监控管理器接口
 * 负责统一管理所有监控组件，提供监控系统的核心功能
 * 
 * @author muxin
 */
public interface MonitoringManager {
    
    // ========== 组件管理 ==========
    
    /**
     * 注册可监控组件
     * 
     * @param component 可监控组件
     */
    void registerComponent(Monitorable component);
    
    /**
     * 批量注册组件
     * 
     * @param components 组件集合
     */
    void registerComponents(Collection<Monitorable> components);
    
    /**
     * 注销组件
     * 
     * @param monitorId 监控ID
     * @return 是否注销成功
     */
    boolean unregisterComponent(String monitorId);
    
    /**
     * 获取已注册的组件
     * 
     * @param monitorId 监控ID
     * @return 监控组件，不存在则返回null
     */
    Monitorable getComponent(String monitorId);
    
    /**
     * 获取所有已注册的组件
     * 
     * @return 组件集合
     */
    Collection<Monitorable> getRegisteredComponents();
    
    /**
     * 根据类型获取组件
     * 
     * @param type 监控类型
     * @return 该类型的所有组件
     */
    Collection<Monitorable> getComponentsByType(MonitorType type);
    
    // ========== 注册中心管理 ==========
    
    /**
     * 获取指标注册中心
     * 
     * @return 指标注册中心
     */
    MetricsRegistry getMetricsRegistry();
    
    // ========== 监控数据收集 ==========
    
    /**
     * 收集所有指标快照
     * 
     * @return 指标快照
     */
    MetricsSnapshot collectMetrics();
    
    /**
     * 异步收集指标快照
     * 
     * @return 指标快照的Future
     */
    CompletableFuture<MetricsSnapshot> collectMetricsAsync();

    
    /**
     * 收集指定组件的监控数据
     * 
     * @param monitorId 监控ID
     * @return 监控数据映射
     */
    Map<String, Object> collectComponentData(String monitorId);
    
    // ========== 监控配置 ==========
    
    /**
     * 设置指标收集间隔
     * 
     * @param intervalMillis 间隔时间（毫秒）
     */
    void setMetricsCollectionInterval(long intervalMillis);
    
    /**
     * 设置健康检查间隔
     * 
     * @param intervalMillis 间隔时间（毫秒）
     */
    void setHealthCheckInterval(long intervalMillis);
    
    /**
     * 启用自动数据收集
     * 
     * @param enabled 是否启用
     */
    void setAutoCollectionEnabled(boolean enabled);
    
    /**
     * 是否启用了自动数据收集
     * 
     * @return 是否启用
     */
    boolean isAutoCollectionEnabled();
    
    // ========== 统计信息 ==========
    
    /**
     * 获取注册组件数量
     * 
     * @return 组件数量
     */
    int getComponentCount();
    
    /**
     * 获取按类型统计的组件数量
     * 
     * @return 类型到数量的映射
     */
    Map<MonitorType, Integer> getComponentCountsByType();
    
    /**
     * 获取监控系统整体状态
     * 
     * @return 状态信息
     */
    Map<String, Object> getSystemStatus();
    
    // ========== 生命周期管理 ==========
    
    /**
     * 启动监控管理器
     */
    void start();
    
    /**
     * 停止监控管理器
     */
    void stop();
    
    /**
     * 检查监控管理器是否运行中
     * 
     * @return 是否运行中
     */
    boolean isRunning();
    
    /**
     * 清理所有监控数据
     */
    void clear();
}