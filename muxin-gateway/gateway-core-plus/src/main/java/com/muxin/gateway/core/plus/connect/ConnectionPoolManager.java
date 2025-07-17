package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.time.Duration;
import java.util.Set;

/**
 * 连接池管理器接口
 * 统一管理所有协议的连接池，简化网关的连接管理
 *
 * @author muxin
 */
public interface ConnectionPoolManager extends LifeCycle {

    // ========== 客户端连接管理（用于后端调用）==========

    /**
     * 获取客户端连接 - 同步
     *
     * @param target   目标地址
     * @param protocol 协议类型
     * @return 客户端连接
     * @throws RuntimeException 如果获取连接失败
     */
    ClientConnection getClientConnection(EndpointAddress target, Protocol protocol);

    /**
     * 获取客户端连接 - 带超时
     *
     * @param target   目标地址
     * @param protocol 协议类型
     * @param timeout  超时时间
     * @return 客户端连接
     * @throws RuntimeException 如果获取连接失败或超时
     */
    ClientConnection getClientConnection(EndpointAddress target,
                                         Protocol protocol,
                                         Duration timeout);

    // ========== 连接生命周期管理 ==========

    /**
     * 归还连接到池中（可复用）
     *
     * @param connection 要归还的连接
     */
    void returnConnection(Connection connection);

    /**
     * 释放连接（销毁，不可复用）
     *
     * @param connection 要释放的连接
     */
    void releaseConnection(Connection connection);

    // ========== 连接池管理 ==========

    /**
     * 预热指定目标的连接池
     *
     * @param target         目标地址
     * @param protocol       协议类型
     * @param minConnections 最小连接数
     */
    void warmupPool(EndpointAddress target, Protocol protocol, int minConnections);

    /**
     * 移除指定目标的连接池
     *
     * @param target   目标地址
     * @param protocol 协议类型
     */
    void removePool(EndpointAddress target, Protocol protocol);

    /**
     * 清理空闲连接
     */
    void cleanupIdleConnections();

    /**
     * 清理不健康的连接池
     */
    void cleanupUnhealthyPools();

    /**
     * 获取连接池数量
     *
     * @return 连接池总数
     */
    int getPoolCount();

    /**
     * 检查是否支持指定协议
     *
     * @param protocol 协议类型
     * @return 是否支持
     */
    boolean supportsProtocol(Protocol protocol);

    /**
     * 获取所有支持的协议
     *
     * @return 支持的协议集合
     */
    Set<Protocol> getSupportedProtocols();

} 