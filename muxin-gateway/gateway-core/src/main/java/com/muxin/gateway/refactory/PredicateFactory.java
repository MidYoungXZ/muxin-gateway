package com.muxin.gateway.refactory;

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
    UniversalPredicate createPathPredicate(String pattern);
    
    /**
     * 创建协议断言
     */
    UniversalPredicate createProtocolPredicate(Protocol protocol);
    
    /**
     * 创建头部断言
     */
    UniversalPredicate createHeaderPredicate(String headerName, String expectedValue);
    
    /**
     * 创建方法断言
     */
    UniversalPredicate createMethodPredicate(String... methods);
    
    /**
     * 创建复合断言 (AND)
     */
    UniversalPredicate createAndPredicate(UniversalPredicate... predicates);
    
    /**
     * 创建复合断言 (OR)
     */
    UniversalPredicate createOrPredicate(UniversalPredicate... predicates);
    
    /**
     * 创建取反断言 (NOT)
     */
    UniversalPredicate createNotPredicate(UniversalPredicate predicate);
    
    /**
     * 根据配置创建断言
     */
    UniversalPredicate createFromConfig(String type, Map<String, Object> config);
    
    /**
     * 注册自定义断言类型
     */
    void registerPredicateType(String type, PredicateCreator creator);
    
    /**
     * 断言创建器接口
     */
    @FunctionalInterface
    interface PredicateCreator {
        UniversalPredicate create(Map<String, Object> config);
    }
} 