package com.muxin.gateway.refactory;

import java.util.concurrent.CompletableFuture;

/**
 * 网关核心处理器
 * 负责协调各层组件完成请求处理流程
 *
 * @author muxin
 */
public interface GatewayProcessor {
    
    /**
     * 处理请求
     *
     * @param context 请求上下文
     * @return 处理结果的Future
     */
    CompletableFuture<Void> processRequest(UniversalRequestContext context);
    
    /**
     * 处理响应
     *
     * @param context 请求上下文
     * @return 处理结果的Future
     */
    CompletableFuture<Void> processResponse(UniversalRequestContext context);
    
    /**
     * 处理异常
     *
     * @param context 请求上下文
     * @param exception 异常
     */
    void processError(UniversalRequestContext context, Exception exception);
    
    /**
     * 获取路由管理器
     */
    RouteManager getRouteManager();
    
    /**
     * 获取过滤器管理器
     */
    FilterManager getFilterManager();
    
    /**
     * 获取负载均衡管理器
     */
    LoadBalanceManager getLoadBalanceManager();
    
    /**
     * 获取节点管理器
     */
    NodeManager getNodeManager();
} 