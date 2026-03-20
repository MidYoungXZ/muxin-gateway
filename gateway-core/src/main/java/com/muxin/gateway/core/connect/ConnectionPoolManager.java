package com.muxin.gateway.core.connect;

import com.muxin.gateway.core.common.LifeCycle;
import com.muxin.gateway.core.service.EndpointAddress;

/**
 * HTTP 连接池管理器接口
 * 简化版本：只支持 HTTP 协议
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ConnectionPoolManager extends LifeCycle {

    ClientConnection getClientConnection(EndpointAddress target);

    ClientConnection getClientConnection(EndpointAddress target, long timeoutMs);

    void returnConnection(Connection connection);

    void releaseConnection(Connection connection);

    void warmupPool(EndpointAddress target, int minConnections);

    void removePool(EndpointAddress target);

    void cleanupIdleConnections();

    void cleanupUnhealthyPools();

    int getPoolCount();
}
