package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.Map;

/**
 * HTTP连接池接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ConnectionPool extends LifeCycle {

    Connection getConnection(EndpointAddress target);

    Connection getConnection(EndpointAddress target, long timeoutMs);

    void releaseConnection(Connection connection);

    void removeConnection(Connection connection);

    int getActiveCount();

    int getIdleCount();

    int getTotalCount();

    Map<String, Object> getPoolStatus(EndpointAddress target);

    void warmup(EndpointAddress target, int minConnections);

    void cleanupIdleConnections();
}
