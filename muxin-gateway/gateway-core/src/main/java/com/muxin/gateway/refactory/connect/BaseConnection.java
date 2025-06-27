package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基础连接接口 - 协议无关的连接抽象
 * 定义了所有类型连接的通用功能和属性
 * 
 * @author muxin
 * @since 2.0
 */
public interface BaseConnection {

    /**
     * 连接唯一标识符
     * 
     * @return 连接ID
     */
    String getConnectionId();

    /**
     * 连接使用的协议
     * 
     * @return 协议信息
     */
    Protocol getProtocol();

    /**
     * 本地端点地址
     * 
     * @return 本地地址
     */
    EndpointAddress getLocalAddress();

    /**
     * 远程端点地址
     * 
     * @return 远程地址
     */
    EndpointAddress getRemoteAddress();

    /**
     * 连接当前状态
     * 
     * @return 连接状态
     */
    ConnectionStatus getStatus();

    /**
     * 检查连接是否处于活跃状态
     * 
     * @return 是否活跃
     */
    boolean isActive();

    /**
     * 异步关闭连接
     * 
     * @return 关闭操作的Future
     */
    CompletableFuture<Void> close();

    /**
     * 获取所有连接属性
     * 
     * @return 连接属性Map
     */
    Map<String, Object> getAttributes();

    /**
     * 设置连接属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    void setAttribute(String key, Object value);

    /**
     * 获取指定类型的连接属性
     * 
     * @param key 属性键
     * @param type 属性值类型
     * @param <T> 类型参数
     * @return 属性值，如果不存在或类型不匹配则返回null
     */
    <T> T getAttribute(String key, Class<T> type);

    /**
     * 添加连接监听器
     * 
     * @param listener 连接监听器
     */
    void addListener(ConnectionListener listener);

    /**
     * 移除连接监听器
     * 
     * @param listener 连接监听器
     */
    void removeListener(ConnectionListener listener);

    /**
     * 获取所有连接监听器
     * 
     * @return 监听器列表
     */
    List<ConnectionListener> getListeners();

    /**
     * 获取连接创建时间
     * 
     * @return 创建时间戳（毫秒）
     */
    long getCreatedTime();

    /**
     * 获取连接最后活跃时间
     * 
     * @return 最后活跃时间戳（毫秒）
     */
    long getLastActiveTime();

    /**
     * 更新最后活跃时间为当前时间
     */
    void updateLastActiveTime();

    /**
     * 获取连接的统计信息
     * 
     * @return 连接统计信息
     */
    ConnectionStatistics getStatistics();

    /**
     * 连接统计信息
     */
    interface ConnectionStatistics {
        
        /**
         * 发送的字节数
         */
        long getBytesSent();
        
        /**
         * 接收的字节数
         */
        long getBytesReceived();
        
        /**
         * 发送的消息数
         */
        long getMessagesSent();
        
        /**
         * 接收的消息数
         */
        long getMessagesReceived();
        
        /**
         * 连接错误次数
         */
        long getErrorCount();
        
        /**
         * 连接持续时间（毫秒）
         */
        long getDuration();
        
        /**
         * 平均延迟（毫秒）
         */
        double getAverageLatency();
    }
} 