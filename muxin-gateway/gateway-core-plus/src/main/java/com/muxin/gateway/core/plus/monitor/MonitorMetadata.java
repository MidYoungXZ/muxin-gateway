package com.muxin.gateway.core.plus.monitor;

import java.util.Map;

/**
 * 监控元数据接口
 * 提供监控组件的元信息
 * 
 * @author muxin
 */
public interface MonitorMetadata {
    
    /**
     * 获取组件名称
     * 
     * @return 组件名称
     */
    String getComponentName();
    
    /**
     * 获取组件版本
     * 
     * @return 版本号
     */
    String getVersion();
    
    /**
     * 获取组件描述
     * 
     * @return 组件描述
     */
    String getDescription();
    
    /**
     * 获取组件标签
     * 
     * @return 标签映射
     */
    Map<String, String> getTags();
    
    /**
     * 获取扩展属性
     * 
     * @return 属性映射
     */
    Map<String, Object> getProperties();
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间戳（毫秒）
     */
    long getCreatedTime();
    
    /**
     * 获取最后更新时间
     * 
     * @return 更新时间戳（毫秒）
     */
    long getLastUpdateTime();
    
    /**
     * 获取组件状态
     * 
     * @return 组件状态（ACTIVE, INACTIVE, ERROR等）
     */
    String getStatus();
    
    /**
     * 获取组件负责人
     * 
     * @return 负责人信息
     */
    String getOwner();
    
    /**
     * 获取组件环境信息
     * 
     * @return 环境标识（dev, test, prod等）
     */
    String getEnvironment();
}