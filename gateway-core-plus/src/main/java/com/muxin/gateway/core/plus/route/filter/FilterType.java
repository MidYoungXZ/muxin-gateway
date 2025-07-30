package com.muxin.gateway.core.plus.route.filter;

/**
 * 过滤器类型枚举
 *
 * @author muxin
 */
public enum FilterType {
    
    /**
     * 前置过滤器 - 在路由匹配前执行
     */
    PRE,
    
    /**
     * 路由过滤器 - 在路由匹配后执行
     */
    ROUTE,
    
    /**
     * 后置过滤器 - 在后端调用后执行
     */
    POST,
    
    /**
     * 错误过滤器 - 在出现错误时执行
     */
    ERROR
} 