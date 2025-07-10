package com.muxin.gateway.core.plus.protocol.message;

import com.muxin.gateway.core.plus.route.RequestContext;

/**
 * 协议转换器接口 - 纯粹的协议转换功能
 * 负责将协议特定的数据转换为统一消息格式，以及反向转换
 * 
 * @author muxin
 * @since 2.0
 */
public interface ProtocolConverter {
    
    /**
     * 支持的源协议
     * 
     * @return 源协议类型
     */
    Protocol getSupportedSourceProtocol();
    
    /**
     * 支持的目标协议
     * 
     * @return 目标协议类型
     */
    Protocol getSupportedTargetProtocol();
    
    /**
     * 将协议特定数据转换为统一消息格式
     * 
     * @param protocolSpecific 协议特定的数据对象
     * @param context 请求上下文
     * @return 统一消息对象
     * @throws ProtocolConversionException 转换失败时抛出
     */
    Message convertToUniversal(Object protocolSpecific, RequestContext context)
            throws ProtocolConversionException;
    
    /**
     * 将统一消息格式转换为协议特定数据
     * 
     * @param universal 统一消息对象
     * @param context 请求上下文
     * @return 协议特定的数据对象
     * @throws ProtocolConversionException 转换失败时抛出
     */
    Object convertFromUniversal(Message universal, RequestContext context)
            throws ProtocolConversionException;
    
    /**
     * 检查是否支持指定的协议转换
     * 
     * @param sourceProtocol 源协议
     * @param targetProtocol 目标协议
     * @return 是否支持转换
     */
    boolean supports(Protocol sourceProtocol, Protocol targetProtocol);
    
    /**
     * 获取转换器的性能指标
     * 
     * @return 转换性能指标
     */
    ConversionMetrics getMetrics();
    
    /**
     * 协议转换异常
     */
    class ProtocolConversionException extends RuntimeException {
        
        private final Protocol sourceProtocol;
        private final Protocol targetProtocol;
        
        public ProtocolConversionException(String message, Protocol sourceProtocol, Protocol targetProtocol) {
            super(message);
            this.sourceProtocol = sourceProtocol;
            this.targetProtocol = targetProtocol;
        }
        
        public ProtocolConversionException(String message, Throwable cause, 
                                         Protocol sourceProtocol, Protocol targetProtocol) {
            super(message, cause);
            this.sourceProtocol = sourceProtocol;
            this.targetProtocol = targetProtocol;
        }
        
        public Protocol getSourceProtocol() {
            return sourceProtocol;
        }
        
        public Protocol getTargetProtocol() {
            return targetProtocol;
        }
    }
    
    /**
     * 转换性能指标
     */
    interface ConversionMetrics {
        
        /**
         * 总转换次数
         */
        long getTotalConversions();
        
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
         * 成功率
         */
        default double getSuccessRate() {
            long total = getTotalConversions();
            return total > 0 ? (double) getSuccessfulConversions() / total : 0.0;
        }
    }
} 