package com.muxin.gateway.core.plus.route.filter;

/**
 * 过滤器工厂接口
 * 每种Filter类型有对应的Factory实现
 * 
 * @author muxin
 */
public interface FilterFactory {
    
    /**
     * 创建Filter实例（每次调用创建新实例）
     * Filter实例与Route绑定，不同Route即使配置相同也是独立实例
     */
    Filter createFilter(FilterDefinition definition);
    
    /**
     * 获取支持的Filter名称
     */
    String getSupportedFilterName();
    
    /**
     * 验证配置参数
     * 在创建Filter前进行配置验证
     */
    void validateConfig(FilterDefinition definition);
} 