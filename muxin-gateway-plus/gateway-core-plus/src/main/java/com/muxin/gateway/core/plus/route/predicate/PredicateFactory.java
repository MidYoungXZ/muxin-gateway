package com.muxin.gateway.core.plus.route.predicate;

/**
 * 断言工厂接口
 * 每种Predicate类型有对应的Factory实现
 * 参考FilterFactory的设计模式
 * 
 * @author muxin
 */
public interface PredicateFactory {
    
    /**
     * 创建Predicate实例（每次调用创建新实例）
     * Predicate实例与Route绑定，不同Route即使配置相同也是独立实例
     */
    Predicate createPredicate(PredicateDefinition definition);
    
    /**
     * 获取支持的Predicate名称
     */
    String getSupportedPredicateName();
    
    /**
     * 验证配置参数
     * 在创建Predicate前进行配置验证
     */
    void validateConfig(PredicateDefinition definition);
} 