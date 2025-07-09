package com.muxin.gateway.core.plus.route;

/**
 * 路由目标工厂接口
 * 负责根据配置创建具体的路由目标实现
 *
 * @author muxin
 */
public interface RouteTargetFactory {
    
    /**
     * 创建路由目标实例
     * @param definition 路由目标配置
     * @return 路由目标实例
     */
    RouteTarget createRouteTarget(RouteTargetDefinition definition);
    
    /**
     * 获取支持的目标类型
     * @return 目标类型
     */
    TargetType getSupportedType();
    
    /**
     * 验证配置参数
     * 在创建RouteTarget前进行配置验证
     * @param definition 路由目标配置
     */
    void validateConfig(RouteTargetDefinition definition);
} 