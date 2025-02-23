package com.muxin.gateway.core.predicate;

import com.muxin.gateway.core.http.ServerWebExchange;

import java.util.function.Predicate;

/**
 * 定义了一个路由谓词接口，继承自 {@link Predicate<ServerWebExchange>}。
 * 该接口用于根据传入的 {@link ServerWebExchange} 对象判断是否匹配某个路由规则。
 *
 * @author Administrator
 * @date 2024/11/19 14:48
 */
public interface RoutePredicate extends Predicate<ServerWebExchange> {

}
