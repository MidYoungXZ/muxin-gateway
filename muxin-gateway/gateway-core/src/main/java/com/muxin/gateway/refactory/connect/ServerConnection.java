package com.muxin.gateway.refactory.connect;

import java.util.concurrent.CompletableFuture;

/**
 * 服务器端连接接口 - 用于处理入站连接
 * 服务器端连接只能响应客户端请求，不支持主动发送消息
 * 
 * @author muxin
 * @since 2.0
 */
public interface ServerConnection extends BaseConnection {

    /**
     * 向客户端写入协议特定的响应数据
     * 注意：传入的响应对象应该是已经转换好的协议特定格式（如FullHttpResponse）
     * 
     * @param protocolSpecificResponse 协议特定的响应对象
     * @return 写入操作的Future
     */
    CompletableFuture<Void> writeResponse(Object protocolSpecificResponse);

    /**
     * 向客户端写入原始字节数据
     * 
     * @param data 原始响应数据
     * @return 写入操作的Future
     */
    CompletableFuture<Void> writeRawData(byte[] data);

    /**
     * 刷新输出缓冲区，确保数据立即发送
     * 
     * @return 刷新操作的Future
     */
    CompletableFuture<Void> flush();

    /**
     * 获取协议特定的上下文对象
     * 例如：对于Netty实现，返回ChannelHandlerContext
     * 
     * @param contextType 上下文类型
     * @param <T> 类型参数
     * @return 协议特定的上下文对象，如果不支持则返回null
     */
    <T> T getProtocolContext(Class<T> contextType);

    /**
     * 检查连接是否支持持久连接
     * 
     * @return 是否支持持久连接
     */
    boolean supportsPersistentConnection();

    /**
     * 设置连接是否保持持久连接
     * 
     * @param keepAlive 是否保持连接
     */
    void setKeepAlive(boolean keepAlive);

    /**
     * 检查连接是否保持持久连接
     * 
     * @return 是否保持连接
     */
    boolean isKeepAlive();

    /**
     * 获取连接的读取超时时间（毫秒）
     * 
     * @return 读取超时时间，-1表示不限制
     */
    long getReadTimeout();

    /**
     * 设置连接的读取超时时间
     * 
     * @param timeoutMs 读取超时时间（毫秒），-1表示不限制
     */
    void setReadTimeout(long timeoutMs);

    /**
     * 获取连接的写入超时时间（毫秒）
     * 
     * @return 写入超时时间，-1表示不限制
     */
    long getWriteTimeout();

    /**
     * 设置连接的写入超时时间
     * 
     * @param timeoutMs 写入超时时间（毫秒），-1表示不限制
     */
    void setWriteTimeout(long timeoutMs);

    /**
     * 检查连接是否可读
     * 
     * @return 是否可读
     */
    boolean isReadable();

    /**
     * 检查连接是否可写
     * 
     * @return 是否可写
     */
    boolean isWritable();

    /**
     * 获取写入缓冲区的当前大小
     * 
     * @return 缓冲区大小（字节）
     */
    int getWriteBufferSize();

    /**
     * 获取写入缓冲区的高水位标记
     * 当缓冲区大小超过此值时，连接变为不可写状态
     * 
     * @return 高水位标记（字节）
     */
    int getWriteBufferHighWaterMark();

    /**
     * 获取写入缓冲区的低水位标记
     * 当缓冲区大小低于此值时，连接恢复可写状态
     * 
     * @return 低水位标记（字节）
     */
    int getWriteBufferLowWaterMark();

    /**
     * 获取客户端的用户代理信息（如果可用）
     * 
     * @return 用户代理字符串，如果不可用则返回null
     */
    String getUserAgent();

    /**
     * 获取连接的安全级别
     * 
     * @return 安全级别
     */
    SecurityLevel getSecurityLevel();

    /**
     * 检查连接是否使用安全传输（如TLS/SSL）
     * 
     * @return 是否使用安全传输
     */
    default boolean isSecure() {
        return getSecurityLevel() != SecurityLevel.NONE;
    }

    /**
     * 获取服务器端连接的特定统计信息
     * 
     * @return 服务器端连接统计信息
     */
    @Override
    ServerConnectionStatistics getStatistics();

    /**
     * 安全级别枚举
     */
    enum SecurityLevel {
        /**
         * 无安全保护
         */
        NONE,
        /**
         * 基本安全保护（如TLS 1.0/1.1）
         */
        BASIC,
        /**
         * 高级安全保护（如TLS 1.2）
         */
        HIGH,
        /**
         * 最高安全保护（如TLS 1.3）
         */
        MAXIMUM
    }

    /**
     * 服务器端连接统计信息
     */
    interface ServerConnectionStatistics extends ConnectionStatistics {
        
        /**
         * 接收的请求数
         */
        long getRequestsReceived();
        
        /**
         * 发送的响应数
         */
        long getResponsesSent();
        
        /**
         * 连接的并发处理峰值
         */
        int getPeakConcurrentRequests();
        
        /**
         * 平均请求处理时间（毫秒）
         */
        double getAverageRequestProcessingTime();
        
        /**
         * 写入缓冲区溢出次数
         */
        long getBufferOverflowCount();
        
        /**
         * 连接保持时长（毫秒）
         */
        long getConnectionDuration();
        
        /**
         * 响应成功率
         */
        default double getResponseSuccessRate() {
            long received = getRequestsReceived();
            long sent = getResponsesSent();
            return received > 0 ? (double) sent / received : 0.0;
        }
    }
} 