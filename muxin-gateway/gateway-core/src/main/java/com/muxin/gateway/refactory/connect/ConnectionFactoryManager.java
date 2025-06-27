package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.LifeCycle;
import com.muxin.gateway.refactory.message.Protocol;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 连接工厂管理器接口
 * 负责管理所有协议的连接工厂，提供统一的连接工厂访问入口
 * 
 * @author muxin
 * @since 2.0
 */
public interface ConnectionFactoryManager extends LifeCycle {

    /**
     * 获取指定协议的连接工厂
     * 
     * @param protocol 协议类型
     * @return 连接工厂，如果不存在则返回null
     */
    ConnectionFactory getFactory(Protocol protocol);

    /**
     * 获取指定协议的连接工厂（Optional包装）
     * 
     * @param protocol 协议类型
     * @return 连接工厂的Optional
     */
    default Optional<ConnectionFactory> getFactoryOptional(Protocol protocol) {
        return Optional.ofNullable(getFactory(protocol));
    }

    /**
     * 注册连接工厂
     * 
     * @param factory 连接工厂
     * @throws IllegalArgumentException 如果工厂为null或已存在相同协议的工厂
     */
    void registerFactory(ConnectionFactory factory);

    /**
     * 注销连接工厂
     * 
     * @param protocol 协议类型
     * @return 被注销的工厂，如果不存在则返回null
     */
    ConnectionFactory unregisterFactory(Protocol protocol);

    /**
     * 检查是否支持指定协议
     * 
     * @param protocol 协议类型
     * @return 是否支持
     */
    boolean supports(Protocol protocol);

    /**
     * 获取所有支持的协议
     * 
     * @return 支持的协议集合
     */
    Set<Protocol> getSupportedProtocols();

    /**
     * 获取所有已注册的连接工厂
     * 
     * @return 连接工厂列表
     */
    List<ConnectionFactory> getAllFactories();

    /**
     * 获取指定协议类型的所有工厂
     * 例如：获取所有HTTP协议的工厂（可能有HTTP/1.1和HTTP/2）
     * 
     * @param protocolName 协议名称
     * @return 匹配的工厂列表
     */
    List<ConnectionFactory> getFactoriesByProtocolName(String protocolName);

    /**
     * 预热所有连接工厂
     * 
     * @return 预热操作结果，true表示所有工厂预热成功
     */
    boolean warmupAllFactories();

    /**
     * 预热指定协议的连接工厂
     * 
     * @param protocol 协议类型
     * @return 预热操作结果，true表示预热成功
     */
    boolean warmupFactory(Protocol protocol);

    /**
     * 关闭所有连接工厂
     * 
     * @return 关闭操作结果，true表示所有工厂关闭成功
     */
    boolean shutdownAllFactories();

    /**
     * 关闭指定协议的连接工厂
     * 
     * @param protocol 协议类型
     * @return 关闭操作结果，true表示关闭成功
     */
    boolean shutdownFactory(Protocol protocol);

    /**
     * 获取管理器的统计信息
     * 
     * @return 管理器统计信息
     */
    FactoryManagerStats getStats();

    /**
     * 清除所有连接工厂
     */
    void clearAll();

    /**
     * 检查指定工厂的健康状态
     * 
     * @param protocol 协议类型
     * @return 健康状态
     */
    Optional<ConnectionFactory.ConnectionHealthStatus> getFactoryHealth(Protocol protocol);

    /**
     * 获取所有工厂的健康状态
     * 
     * @return 协议到健康状态的映射
     */
    java.util.Map<Protocol, ConnectionFactory.ConnectionHealthStatus> getAllFactoryHealth();

    /**
     * 重新加载指定协议的工厂配置
     * 
     * @param protocol 协议类型
     * @return 重新加载是否成功
     */
    boolean reloadFactoryConfig(Protocol protocol);

    /**
     * 重新加载所有工厂配置
     * 
     * @return 重新加载是否成功
     */
    boolean reloadAllFactoryConfigs();

    /**
     * 设置工厂事件监听器
     * 
     * @param listener 事件监听器
     */
    void setFactoryEventListener(FactoryEventListener listener);

    /**
     * 移除工厂事件监听器
     */
    void removeFactoryEventListener();

    /**
     * 工厂管理器统计信息
     */
    interface FactoryManagerStats {
        
        /**
         * 已注册的工厂数量
         */
        int getRegisteredFactoriesCount();
        
        /**
         * 支持的协议数量
         */
        int getSupportedProtocolsCount();
        
        /**
         * 活跃的工厂数量
         */
        int getActiveFactoriesCount();
        
        /**
         * 总的连接创建次数
         */
        long getTotalConnectionsCreated();
        
        /**
         * 总的连接创建失败次数
         */
        long getTotalConnectionCreationFailures();
        
        /**
         * 平均连接创建时间（毫秒）
         */
        double getAverageConnectionCreationTime();
        
        /**
         * 最常使用的协议
         */
        Optional<Protocol> getMostUsedProtocol();
        
        /**
         * 管理器启动时间
         */
        long getManagerStartupTime();
        
        /**
         * 连接创建成功率
         */
        default double getConnectionCreationSuccessRate() {
            long total = getTotalConnectionsCreated();
            long failures = getTotalConnectionCreationFailures();
            return total > 0 ? (double) (total - failures) / total : 0.0;
        }
    }

    /**
     * 工厂事件监听器
     */
    interface FactoryEventListener {
        
        /**
         * 工厂注册事件
         * 
         * @param protocol 协议类型
         * @param factory 连接工厂
         */
        default void onFactoryRegistered(Protocol protocol, ConnectionFactory factory) {}
        
        /**
         * 工厂注销事件
         * 
         * @param protocol 协议类型
         * @param factory 连接工厂
         */
        default void onFactoryUnregistered(Protocol protocol, ConnectionFactory factory) {}
        
        /**
         * 工厂状态变更事件
         * 
         * @param protocol 协议类型
         * @param oldStatus 旧状态
         * @param newStatus 新状态
         */
        default void onFactoryStatusChanged(Protocol protocol, 
                                          ConnectionFactory.ConnectionHealthStatus oldStatus,
                                          ConnectionFactory.ConnectionHealthStatus newStatus) {}
        
        /**
         * 工厂预热完成事件
         * 
         * @param protocol 协议类型
         * @param success 是否成功
         */
        default void onFactoryWarmupCompleted(Protocol protocol, boolean success) {}
        
        /**
         * 工厂关闭事件
         * 
         * @param protocol 协议类型
         * @param success 是否成功
         */
        default void onFactoryShutdown(Protocol protocol, boolean success) {}
        
        /**
         * 工厂错误事件
         * 
         * @param protocol 协议类型
         * @param error 错误信息
         */
        default void onFactoryError(Protocol protocol, Throwable error) {}
    }
} 