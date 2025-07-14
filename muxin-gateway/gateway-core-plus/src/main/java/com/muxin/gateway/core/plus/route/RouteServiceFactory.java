package com.muxin.gateway.core.plus.route;

/**
 * 路由目标工厂接口
 * 负责根据服务定义创建具体的路由目标实现
 *
 * @author muxin
 */
public interface RouteServiceFactory {
    
    /**
     * 创建路由目标实例
     * @param serviceDefinition 服务定义配置
     * @return 路由目标实例
     */
    RouteService createRouteTarget(ServiceDefinition serviceDefinition);
    
    /**
     * 获取支持的目标类型
     * @return 目标类型
     */
    ServiceType getSupportedType();
    
    /**
     * 验证配置参数
     * 在创建RouteTarget前进行配置验证
     * @param serviceDefinition 服务定义配置
     */
    void validateConfig(ServiceDefinition serviceDefinition);
} 