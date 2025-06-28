package com.muxin.gateway.core.plus.filter;

import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Set;

/**
 * 通用过滤器接口 - 支持多协议
 *
 * @author muxin
 */
public interface Filter {
    
    /**
     * 过滤器执行
     */
    void filter(RequestContext context, FilterChain chain);
    
    /**
     * 过滤器名称
     */
    String getName();
    
    /**
     * 过滤器类型
     */
    FilterType getType();
    
    /**
     * 执行顺序（数值越小优先级越高）
     */
    int getOrder();
    
    /**
     * 是否启用
     */
    boolean isEnabled();
    
    /**
     * 支持的协议
     */
    Set<Protocol> getSupportedProtocols();
} 