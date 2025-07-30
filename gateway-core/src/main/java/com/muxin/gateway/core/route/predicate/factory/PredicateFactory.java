package com.muxin.gateway.core.route.predicate.factory;

import com.muxin.gateway.core.route.RoutePredicate;

import java.util.Map;

/**
 * 断言工厂接口
 */
public interface PredicateFactory {

    /**
     * 创建路由断言
     * 
     * @param args 断言参数
     * @return 路由断言
     */
    RoutePredicate create(Map<String, String> args);

    /**
     * 获取断言名称
     * 
     * @return 断言名称
     */
    String name();
} 