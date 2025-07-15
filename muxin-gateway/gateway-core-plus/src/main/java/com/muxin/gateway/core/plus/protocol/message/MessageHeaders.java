package com.muxin.gateway.core.plus.protocol.message;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 消息头部接口
 *
 * @author muxin
 */
public interface MessageHeaders {
    
    /**
     * 设置头部值
     */
    void set(String name, Object value);
    
    /**
     * 获取头部值
     */
    <T> T get(String name, Class<T> type);
    
    /**
     * 安全获取头部值
     */
    <T> Optional<T> getOptional(String name, Class<T> type);
    
    /**
     * 检查是否包含指定头部
     */
    boolean contains(String name);
    
    /**
     * 移除头部
     */
    void remove(String name);
    
    /**
     * 获取所有头部名称
     */
    Set<String> getNames();
    
    /**
     * 转换为Map
     */
    Map<String, Object> asMap();

} 