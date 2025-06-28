package com.muxin.gateway.core.plus.message;


import com.muxin.gateway.core.plus.LifeCycle;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 协议转换管理器接口
 * 负责管理所有协议转换器，提供协议转换能力的统一入口
 * 
 * @author muxin
 * @since 2.0
 */
public interface ProtocolConverterManager extends LifeCycle {
    
    /**
     * 获取指定协议转换的转换器
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 协议转换器，如果不存在则返回null
     */
    ProtocolConverter getConverter(Protocol sourceProtocol, Protocol targetProtocol);
    
    /**
     * 注册协议转换器
     * 
     * @param converter 协议转换器
     * @throws IllegalArgumentException 如果转换器为null或已存在相同转换规则的转换器
     */
    void registerConverter(ProtocolConverter converter);
    
    /**
     * 注销协议转换器
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 被注销的转换器，如果不存在则返回null
     */
    ProtocolConverter unregisterConverter(Protocol sourceProtocol, Protocol targetProtocol);
    
    /**
     * 检查是否可以进行指定的协议转换
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 是否可以转换
     */
    boolean canConvert(Protocol sourceProtocol, Protocol targetProtocol);
    
    /**
     * 获取协议转换链（支持多步转换）
     * 例如：HTTP -> Universal -> gRPC
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 转换链，如果无法转换则返回空列表
     */
    List<ProtocolConverter> getConversionChain(Protocol sourceProtocol, Protocol targetProtocol);
    
    /**
     * 获取所有支持的源协议
     * 
     * @return 支持的源协议集合
     */
    Set<Protocol> getSupportedSourceProtocols();
    
    /**
     * 获取所有支持的目标协议
     * 
     * @return 支持的目标协议集合
     */
    Set<Protocol> getSupportedTargetProtocols();
    
    /**
     * 获取指定协议可以转换到的所有目标协议
     * 
     * @param sourceProtocol 源协议
     * @return 可转换的目标协议集合
     */
    Set<Protocol> getConvertibleTargets(Protocol sourceProtocol);
    
    /**
     * 获取可以转换到指定协议的所有源协议
     * 
     * @param targetProtocol 目标协议
     * @return 可转换的源协议集合
     */
    Set<Protocol> getConvertibleSources(Protocol targetProtocol);
    
    /**
     * 获取所有已注册的转换器
     * 
     * @return 转换器列表
     */
    List<ProtocolConverter> getAllConverters();
    
    /**
     * 获取转换管理器的统计信息
     * 
     * @return 统计信息
     */
    ConverterManagerStats getStats();
    
    /**
     * 清除所有转换器
     */
    void clearAll();
    
    /**
     * 转换管理器统计信息
     */
    interface ConverterManagerStats {
        
        /**
         * 已注册的转换器数量
         */
        int getRegisteredConvertersCount();
        
        /**
         * 支持的协议转换组合数量
         */
        int getSupportedConversionsCount();
        
        /**
         * 总转换请求次数
         */
        long getTotalConversionRequests();
        
        /**
         * 成功转换次数
         */
        long getSuccessfulConversions();
        
        /**
         * 失败转换次数
         */
        long getFailedConversions();
        
        /**
         * 平均转换时间（毫秒）
         */
        double getAverageConversionTime();
        
        /**
         * 最常用的转换组合
         */
        Optional<String> getMostUsedConversion();
        
        /**
         * 转换成功率
         */
        default double getSuccessRate() {
            long total = getTotalConversionRequests();
            return total > 0 ? (double) getSuccessfulConversions() / total : 0.0;
        }
    }
} 