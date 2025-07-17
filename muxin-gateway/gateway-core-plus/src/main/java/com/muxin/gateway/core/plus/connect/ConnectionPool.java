package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;

/**
 * 连接池接口
 * 负责管理和复用网络连接，提高网关性能
 *
 * @author muxin
 */
public interface ConnectionPool extends LifeCycle {
    
    /**
     * 获取连接
     * 
     * @param target 目标地址
     * @param protocol 协议类型
     * @return 连接
     * @throws RuntimeException 如果获取连接失败
     */
    Connection getConnection(EndpointAddress target, Protocol protocol);
    
    /**
     * 获取连接 - 带超时
     * 
     * @param target 目标地址
     * @param protocol 协议类型
     * @param timeoutMs 超时时间(毫秒)
     * @return 连接
     * @throws RuntimeException 如果获取连接失败或超时
     */
    Connection getConnection(EndpointAddress target, Protocol protocol, long timeoutMs);
    
    /**
     * 归还连接到池中
     * 
     * @param connection 要归还的连接
     */
    void returnConnection(Connection connection);
    
    /**
     * 释放连接(连接出现错误时使用)
     * 
     * @param connection 要释放的连接
     */
    void releaseConnection(Connection connection);
    
    /**
     * 获取连接池统计信息
     * 
     * @return 统计信息Map
     */
    Map<String, Object> getStatistics();
    
    /**
     * 获取指定目标的连接池状态
     * 
     * @param target 目标地址
     * @return 连接池状态信息
     */
    Map<String, Object> getPoolStatus(EndpointAddress target);
    
    /**
     * 预热连接池(为指定目标预创建连接)
     * 
     * @param target 目标地址
     * @param protocol 协议类型
     * @param minConnections 最小连接数
     */
    void warmup(EndpointAddress target, Protocol protocol, int minConnections);
    
    /**
     * 清理空闲连接
     */
    void cleanupIdleConnections();
    
    /**
     * 关闭连接池
     */
    void close();
    
    /**
     * 检查连接池是否已关闭
     * 
     * @return true如果已关闭
     */
    boolean isClosed();
    
    /**
     * 设置连接池配置
     * 
     * @param config 配置参数
     */
    void configure(ConnectionPoolConfig config);
    
    /**
     * 获取连接池配置
     * 
     * @return 当前配置
     */
    ConnectionPoolConfig getConfig();
} 