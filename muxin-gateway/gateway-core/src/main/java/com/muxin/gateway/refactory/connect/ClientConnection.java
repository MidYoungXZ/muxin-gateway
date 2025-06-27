package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Message;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 客户端连接接口 - 支持主动发送消息
 * 用于网关向后端服务发起连接和通信
 * 
 * @author muxin
 * @since 2.0
 */
public interface ClientConnection extends BaseConnection {

    /**
     * 异步发送消息（单向发送，不等待响应）
     * 
     * @param message 要发送的消息
     * @return 发送操作的Future，完成时表示消息已发送
     */
    CompletableFuture<Void> send(Message message);

    /**
     * 异步发送消息并等待响应（请求-响应模式）
     * 
     * @param message 要发送的消息
     * @param timeout 响应超时时间
     * @return 响应消息的Future
     */
    CompletableFuture<Message> sendAndReceive(Message message, Duration timeout);

    /**
     * 异步发送消息并等待响应（使用默认超时时间）
     * 
     * @param message 要发送的消息
     * @return 响应消息的Future
     */
    default CompletableFuture<Message> sendAndReceive(Message message) {
        return sendAndReceive(message, Duration.ofSeconds(30));
    }

    /**
     * 检查连接是否可以被连接池复用
     * 
     * @return 是否可池化
     */
    boolean isPoolable();

    /**
     * 标记连接是否可以被连接池复用
     * 
     * @param poolable 是否可池化
     */
    void setPoolable(boolean poolable);

    /**
     * 获取连接的最大空闲时间（毫秒）
     * 超过此时间的空闲连接可能被连接池回收
     * 
     * @return 最大空闲时间，-1表示不限制
     */
    long getMaxIdleTime();

    /**
     * 设置连接的最大空闲时间
     * 
     * @param maxIdleTimeMs 最大空闲时间（毫秒），-1表示不限制
     */
    void setMaxIdleTime(long maxIdleTimeMs);

    /**
     * 检查连接是否空闲超时
     * 
     * @return 是否空闲超时
     */
    default boolean isIdleTimeout() {
        long maxIdleTime = getMaxIdleTime();
        if (maxIdleTime <= 0) {
            return false;
        }
        long idleTime = System.currentTimeMillis() - getLastActiveTime();
        return idleTime > maxIdleTime;
    }

    /**
     * 获取连接的权重（用于负载均衡）
     * 
     * @return 连接权重，默认为1
     */
    default int getWeight() {
        return 1;
    }

    /**
     * 设置连接的权重
     * 
     * @param weight 连接权重
     */
    void setWeight(int weight);

    /**
     * 测试连接的可用性
     * 发送一个轻量级的探测消息来验证连接是否正常
     * 
     * @return 连接测试的Future，true表示连接正常
     */
    CompletableFuture<Boolean> ping();

    /**
     * 测试连接的可用性（带超时）
     * 
     * @param timeout 测试超时时间
     * @return 连接测试的Future，true表示连接正常
     */
    CompletableFuture<Boolean> ping(Duration timeout);

    /**
     * 获取当前正在处理的请求数量
     * 
     * @return 活跃请求数
     */
    int getActiveRequestCount();

    /**
     * 获取连接的最大并发请求数
     * 
     * @return 最大并发请求数，-1表示不限制
     */
    int getMaxConcurrentRequests();

    /**
     * 设置连接的最大并发请求数
     * 
     * @param maxConcurrentRequests 最大并发请求数，-1表示不限制
     */
    void setMaxConcurrentRequests(int maxConcurrentRequests);

    /**
     * 检查连接是否过载
     * 
     * @return 是否过载
     */
    default boolean isOverloaded() {
        int maxRequests = getMaxConcurrentRequests();
        if (maxRequests <= 0) {
            return false;
        }
        return getActiveRequestCount() >= maxRequests;
    }

    /**
     * 获取客户端连接的特定统计信息
     * 
     * @return 客户端连接统计信息
     */
    @Override
    ClientConnectionStatistics getStatistics();

    /**
     * 客户端连接统计信息
     */
    interface ClientConnectionStatistics extends ConnectionStatistics {
        
        /**
         * 发送的请求数
         */
        long getRequestsSent();
        
        /**
         * 接收的响应数
         */
        long getResponsesReceived();
        
        /**
         * 超时的请求数
         */
        long getTimeoutRequests();
        
        /**
         * 平均请求响应时间（毫秒）
         */
        double getAverageResponseTime();
        
        /**
         * 最大并发请求数峰值
         */
        int getPeakConcurrentRequests();
        
        /**
         * 连接池使用次数
         */
        long getPoolUsageCount();
        
        /**
         * 成功率
         */
        default double getSuccessRate() {
            long total = getRequestsSent();
            long successful = getResponsesReceived();
            return total > 0 ? (double) successful / total : 0.0;
        }
    }
} 