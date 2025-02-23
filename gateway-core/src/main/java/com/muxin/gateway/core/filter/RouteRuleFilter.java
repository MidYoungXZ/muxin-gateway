package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 定义了一个路由规则过滤器接口，继承自 {@link Ordered}。
 * 该接口用于处理与路由规则相关的过滤逻辑。
 * 实现该接口的类需要实现过滤逻辑，并指定过滤器的类型和顺序。
 *
 * @author Administrator
 * @date 2024/11/19 09:36
 */
public interface RouteRuleFilter extends Ordered {

    /**
     * 执行过滤逻辑。
     *
     * @param exchange 当前的 {@link ServerWebExchange} 对象
     */
    void filter(ServerWebExchange exchange);

    /**
     * 返回过滤器的类型。
     *
     * @return 过滤器类型 {@link FilterTypeEnum}
     */
    FilterTypeEnum filterType();
}
