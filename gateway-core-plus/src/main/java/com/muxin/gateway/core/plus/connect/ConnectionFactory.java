package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 连接工厂接口 - 负责创建各种类型的网络连接
 * 每个协议都应该有对应的连接工厂实现
 * 
 * @author muxin
 * @since 2.0
 */
public interface ConnectionFactory {

    /**
     * 获取工厂支持的协议
     * 
     * @return 支持的协议
     */
    Protocol getSupportedProtocol();

    /**
     * 检查是否支持指定协议
     * 
     * @param protocol 协议类型
     * @return 是否支持
     */
    boolean supports(Protocol protocol);

    /**
     * 创建服务器端连接
     * 用于处理入站的客户端连接
     * 
     * @param protocolContext 协议特定的上下文对象（如Netty的ChannelHandlerContext）
     * @return 服务器端连接
     * @throws ConnectionCreationException 连接创建失败时抛出
     */
    ServerConnection createServerConnection(Object protocolContext) throws ConnectionCreationException;

    /**
     * 异步创建客户端连接
     * 用于向后端服务发起连接
     * 
     * @param target 目标端点地址
     * @param options 连接选项
     * @return 客户端连接的Future
     */
    CompletableFuture<ClientConnection> createClientConnection(EndpointAddress target, Map<String, Object> options);

    /**
     * 异步创建客户端连接（使用默认选项）
     * 
     * @param target 目标端点地址
     * @return 客户端连接的Future
     */
    default CompletableFuture<ClientConnection> createClientConnection(EndpointAddress target) {
        return createClientConnection(target, getDefaultConnectionOptions());
    }

    /**
     * 验证连接的有效性
     * 
     * @param connection 要验证的连接
     * @return 验证结果的Future，true表示连接有效
     */
    CompletableFuture<Boolean> validateConnection(Connection connection);

    /**
     * 获取连接的健康状态
     * 
     * @param connection 要检查的连接
     * @return 连接健康状态
     */
    ConnectionHealthStatus getConnectionHealth(Connection connection);

    /**
     * 获取默认的连接选项
     * 
     * @return 默认连接选项Map
     */
    Map<String, Object> getDefaultConnectionOptions();

    /**
     * 获取工厂的配置信息
     * 
     * @return 工厂配置
     */
    ConnectionFactoryConfig getConfig();

    /**
     * 更新工厂配置
     * 
     * @param config 新的配置
     */
    void updateConfig(ConnectionFactoryConfig config);

    /**
     * 获取工厂的统计信息
     * 
     * @return 工厂统计信息
     */
    ConnectionFactoryStats getStats();

    /**
     * 预热连接工厂
     * 执行必要的初始化操作，如线程池预热、连接池预创建等
     * 
     * @return 预热操作的Future
     */
    CompletableFuture<Void> warmup();

    /**
     * 关闭连接工厂
     * 释放所有资源，关闭线程池等
     * 
     * @return 关闭操作的Future
     */
    CompletableFuture<Void> shutdown();

    /**
     * 检查工厂是否已关闭
     * 
     * @return 是否已关闭
     */
    boolean isShutdown();

    /**
     * 连接创建异常
     */
    class ConnectionCreationException extends RuntimeException {

        private final Protocol protocol;
        private final EndpointAddress target;

        public ConnectionCreationException(String message, Protocol protocol) {
            super(message);
            this.protocol = protocol;
            this.target = null;
        }

        public ConnectionCreationException(String message, Protocol protocol, EndpointAddress target) {
            super(message);
            this.protocol = protocol;
            this.target = target;
        }

        public ConnectionCreationException(String message, Throwable cause, Protocol protocol, EndpointAddress target) {
            super(message, cause);
            this.protocol = protocol;
            this.target = target;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public EndpointAddress getTarget() {
            return target;
        }
    }

    /**
     * 连接健康状态
     */
    enum ConnectionHealthStatus {
        /**
         * 健康状态
         */
        HEALTHY,
        /**
         * 警告状态（可用但有问题）
         */
        WARNING,
        /**
         * 不健康状态
         */
        UNHEALTHY,
        /**
         * 未知状态
         */
        UNKNOWN
    }

    /**
     * 连接工厂配置
     */
    interface ConnectionFactoryConfig {
        
        /**
         * 获取配置名称
         */
        String getName();
        
        /**
         * 连接超时时间（毫秒）
         */
        default long getConnectionTimeout() {
            return 5000;
        }
        
        /**
         * 是否启用Keep-Alive
         */
        default boolean isKeepAliveEnabled() {
            return true;
        }
        
        /**
         * 获取配置属性
         */
        Map<String, Object> getProperties();
    }

    /**
     * 连接工厂统计信息
     */
    interface ConnectionFactoryStats {
        
        /**
         * 创建的连接总数
         */
        long getConnectionsCreated();
        
        /**
         * 连接创建失败次数
         */
        long getConnectionsFailed();
        
        /**
         * 平均连接创建时间（毫秒）
         */
        double getAverageConnectionTime();
        
        /**
         * 连接创建成功率
         */
        double getSuccessRate();
    }
} 