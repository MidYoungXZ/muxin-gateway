package com.muxin.gateway.core.plus.route.service;

/**
 * 服务节点状态枚举
 *
 * @author muxin
 */
public enum NodeStatus {
    
    /**
     * 节点正常运行，可以接收请求
     */
    HEALTHY("健康", true),
    
    /**
     * 节点不健康，但仍可能接收请求
     */
    UNHEALTHY("不健康", true),
    
    /**
     * 节点暂时不可用，不接收新请求
     */
    UNAVAILABLE("不可用", false),
    
    /**
     * 节点已下线，完全不可用
     */
    OFFLINE("离线", false),
    
    /**
     * 节点正在启动中
     */
    STARTING("启动中", false),
    
    /**
     * 节点正在关闭中
     */
    SHUTTING_DOWN("关闭中", false);
    
    private final String description;
    private final boolean available;
    
    NodeStatus(String description, boolean available) {
        this.description = description;
        this.available = available;
    }
    
    /**
     * 获取状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断该状态下节点是否可用
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * 判断是否为健康状态
     */
    public boolean isHealthy() {
        return this == HEALTHY;
    }
} 