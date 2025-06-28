package com.muxin.gateway.core.plus.monitor;

/**
 * 监控类型枚举
 * 定义系统中各种可监控组件的类型
 * 
 * @author muxin
 */
public enum MonitorType {
    
    /**
     * 网关处理器
     */
    GATEWAY_PROCESSOR("gateway.processor", "网关处理器"),
    
    /**
     * 路由管理器
     */
    ROUTE_MANAGER("route.manager", "路由管理器"),
    
    /**
     * 过滤器管理器
     */
    FILTER_MANAGER("filter.manager", "过滤器管理器"),
    
    /**
     * 负载均衡管理器
     */
    LOAD_BALANCE_MANAGER("loadbalance.manager", "负载均衡管理器"),
    
    /**
     * 节点管理器
     */
    NODE_MANAGER("node.manager", "节点管理器"),
    
    /**
     * 连接池管理器
     */
    CONNECTION_POOL_MANAGER("connection.pool.manager", "连接池管理器"),
    
    /**
     * 连接池
     */
    CONNECTION_POOL("connection.pool", "连接池"),
    
    /**
     * 连接工厂
     */
    CONNECTION_FACTORY("connection.factory", "连接工厂"),
    
    /**
     * 协议转换管理器
     */
    PROTOCOL_CONVERTER_MANAGER("protocol.converter.manager", "协议转换管理器"),
    
    /**
     * HTTP服务器
     */
    HTTP_SERVER("http.server", "HTTP服务器"),
    
    /**
     * 服务节点
     */
    SERVICE_NODE("service.node", "服务节点");
    
    private final String key;
    private final String description;
    
    MonitorType(String key, String description) {
        this.key = key;
        this.description = description;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return key;
    }
} 