package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.http.ServerWebExchange;

import java.util.function.Predicate;

/**
 * 路由断言接口
 * 
 * 扩展Java标准Predicate接口，定义路由匹配的条件
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface RoutePredicate extends Predicate<ServerWebExchange> {

}
