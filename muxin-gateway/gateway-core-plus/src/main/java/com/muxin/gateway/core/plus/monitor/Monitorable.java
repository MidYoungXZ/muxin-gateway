package com.muxin.gateway.core.plus.monitor;

/**
 * 可监控接口 - 所有需要监控的组件都应实现此接口
 * 
 * @author muxin
 */
public interface Monitorable {
    
    /**
     * 获取组件的监控标识符
     * 用于在监控系统中唯一标识该组件
     * 
     * @return 监控标识符
     */
    String getMonitorId();
    
    /**
     * 获取组件类型
     * 用于监控系统分类管理
     * 
     * @return 组件类型
     */
    MonitorType getMonitorType();
    
    /**
     * 注册监控指标
     * 组件启动时调用，向监控系统注册自己的指标
     * 
     * @param registry 指标注册中心
     */
    void registerMetrics(MetricsRegistry registry);
    
    /**
     * 获取监控元数据
     * 
     * @return 监控元数据
     */
    MonitorMetadata getMonitorMetadata();
} 