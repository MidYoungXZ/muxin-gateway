package com.muxin.gateway.core.route.filter.factory;

import com.muxin.gateway.core.route.filter.GatewayFilterChain;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 过滤器链工厂接口
 * 
 * 定义创建GatewayFilterChain的工厂方法
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface FilterChainFactory {

    GatewayFilterChain buildFilterChain(ServerWebExchange serverWebExchange);

}
