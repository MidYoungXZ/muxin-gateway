package com.muxin.gateway.refactory;

import java.util.List;
import java.util.Map;

/**
 * 通用路由接口 - 支持多协议
 *
 * @author muxin
 */
public interface UniversalRoute {
    
    /**
     * 路由ID
     */
    String getId();
    
    /**
     * 路由名称
     */
    String getName();
    
    /**
     * 路由描述
     */
    String getDescription();
    
    /**
     * 路由优先级（数值越小优先级越高）
     */
    int getOrder();
    
    /**
     * 是否启用
     */
    boolean isEnabled();
    
    /**
     * 支持的协议列表
     */
    List<Protocol> getSupportedProtocols();
    
    /**
     * 断言列表（AND关系）
     */
    List<UniversalPredicate> getPredicates();
    
    /**
     * 过滤器列表
     */
    List<UniversalFilter> getFilters();
    
    /**
     * 目标服务配置
     */
    RouteTarget getTarget();
    
    /**
     * 路由元数据
     */
    Map<String, Object> getMetadata();
    
    /**
     * 匹配请求上下文
     */
    boolean matches(UniversalRequestContext context);
} 