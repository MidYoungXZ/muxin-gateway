package com.muxin.gateway.core.factory;

import com.muxin.gateway.core.route.RoutePredicate;

import java.util.Map;

/**
 * 定义了一个谓词工厂接口。该接口用于创建 {@link RoutePredicate} 对象。
 * 实现该接口的类可以根据传入的参数创建具体的路由谓词实例。
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
public interface PredicateFactory {

    /**
     * 根据传入的参数创建一个 {@link RoutePredicate} 对象。
     *
     * @param args 创建谓词所需的参数映射
     * @return 创建的 {@link RoutePredicate} 对象
     */
    RoutePredicate create(Map<String, String> args);
}
