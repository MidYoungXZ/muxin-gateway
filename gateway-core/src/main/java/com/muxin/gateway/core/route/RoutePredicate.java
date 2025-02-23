package com.muxin.gateway.core.route;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 定义了一个路由谓词接口，是一个函数式接口。
 * 该接口用于根据传入的 {@link ServerWebExchange} 对象判断是否匹配某个路由规则。
 *
 * @author Administrator
 * @date 2024/11/19 14:48
 */
@FunctionalInterface
public interface RoutePredicate {

    /**
     * 测试给定的 {@link ServerWebExchange} 是否匹配此谓词。
     *
     * @param exchange 当前的 {@link ServerWebExchange} 对象
     * @return 如果匹配返回 true；否则返回 false
     */
    boolean test(ServerWebExchange exchange);
}
