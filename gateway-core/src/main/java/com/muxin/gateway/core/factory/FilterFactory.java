package com.muxin.gateway.core.factory;

import com.muxin.gateway.core.filter.RouteRuleFilter;

import java.util.Map;

/**
 * 定义了一个过滤器工厂接口。该接口用于创建 {@link RouteRuleFilter} 对象。
 * 实现该接口的类可以根据传入的参数创建具体的过滤器实例。
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
public interface FilterFactory {

    /**
     * 根据传入的参数创建一个 {@link RouteRuleFilter} 对象。
     *
     * @param args 创建过滤器所需的参数映射
     * @return 创建的 {@link RouteRuleFilter} 对象
     */
    RouteRuleFilter create(Map<String, String> args);
}
