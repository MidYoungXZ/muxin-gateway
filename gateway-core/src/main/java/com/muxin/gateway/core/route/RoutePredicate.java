package com.muxin.gateway.core.route;

import com.muxin.gateway.core.http.ServerWebExchange;

@FunctionalInterface
public /**
 * 接口 - 断言
 * 
 * 定义标准契约，实现类必须遵循此接口的规范
 * 
 * @author muxin
 * @since 1.0.0
 */

interface RoutePredicate {
    boolean test(ServerWebExchange exchange);
} 