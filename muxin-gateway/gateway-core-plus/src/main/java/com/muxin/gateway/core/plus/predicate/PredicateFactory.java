package com.muxin.gateway.core.plus.predicate;

import com.muxin.gateway.core.plus.message.Protocol;

import java.util.Map;

/**
 * 断言工厂接口
 * 负责动态创建各种类型的断言
 *
 * @author muxin
 */
public interface PredicateFactory {
    
    /**
     * 创建路径断言
     */
    Predicate createPathPredicate(String pattern);
    
    /**
     * 创建协议断言
     */
    Predicate createProtocolPredicate(Protocol protocol);
    
    /**
     * 创建头部断言
     */
    Predicate createHeaderPredicate(String headerName, String expectedValue);
    
    /**
     * 创建方法断言
     */
    Predicate createMethodPredicate(String... methods);
    
    /**
     * 创建复合断言 (AND)
     */
    Predicate createAndPredicate(Predicate... predicates);
    
    /**
     * 创建复合断言 (OR)
     */
    Predicate createOrPredicate(Predicate... predicates);
    
    /**
     * 创建取反断言 (NOT)
     */
    Predicate createNotPredicate(Predicate predicate);
    
    /**
     * 根据配置创建断言
     */
    Predicate createFromConfig(String type, Map<String, Object> config);
    
    /**
     * 注册自定义断言类型
     */
    void registerPredicateType(String type, PredicateCreator creator);
    
    /**
     * 断言创建器接口
     */
    @FunctionalInterface
    interface PredicateCreator {
        Predicate create(Map<String, Object> config);
    }
} 