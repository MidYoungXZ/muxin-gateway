package com.muxin.gateway.core.plus.route.filter;

/**
 * 过滤器类型枚举
 * 定义网关支持的各种过滤器类型
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum FilterType {
    
    /**
     * 请求ID过滤器 - 生成唯一请求ID
     */
    REQUEST_ID("REQUEST_ID", "请求ID过滤器"),
    
    /**
     * CORS过滤器 - 处理跨域资源共享
     */
    CORS("CORS", "跨域资源共享过滤器"),
    
    /**
     * 请求日志过滤器 - 记录请求日志
     */
    REQUEST_LOG("REQUEST_LOG", "请求日志过滤器"),
    
    /**
     * 认证过滤器 - 处理身份认证
     */
    AUTH("AUTH", "身份认证过滤器"),
    
    /**
     * 指标收集过滤器 - 收集性能指标
     */
    METRICS("METRICS", "性能指标过滤器"),
    
    /**
     * 请求大小限制过滤器 - 限制请求大小
     */
    REQUEST_SIZE_LIMIT("REQUEST_SIZE_LIMIT", "请求大小限制过滤器"),
    
    /**
     * 限流过滤器 - 请求限流控制
     */
    RATE_LIMIT("RATE_LIMIT", "请求限流过滤器"),
    
    /**
     * 默认响应过滤器 - 返回默认响应
     */
    DEFAULT_RESPONSE("DEFAULT_RESPONSE", "默认响应过滤器"),
    
    /**
     * 前置过滤器 - 在路由匹配前执行
     */
    PRE("PRE", "前置过滤器"),
    
    /**
     * 路由过滤器 - 在路由匹配后执行
     */
    ROUTE("ROUTE", "路由过滤器"),
    
    /**
     * 后置过滤器 - 在后端调用后执行
     */
    POST("POST", "后置过滤器"),
    
    /**
     * 错误过滤器 - 在出现错误时执行
     */
    ERROR("ERROR", "错误过滤器");
    
    private final String code;
    private final String description;
    
    FilterType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取过滤器类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取过滤器类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取过滤器类型
     */
    public static FilterType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (FilterType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("不支持的过滤器类型: " + code);
    }
    
    /**
     * 是否为前置过滤器
     */
    public boolean isPre() {
        return this == PRE;
    }
    
    /**
     * 是否为路由过滤器
     */
    public boolean isRoute() {
        return this == ROUTE;
    }
    
    /**
     * 是否为后置过滤器
     */
    public boolean isPost() {
        return this == POST;
    }
    
    /**
     * 是否为错误过滤器
     */
    public boolean isError() {
        return this == ERROR;
    }
    
    /**
     * 是否为功能过滤器（非生命周期过滤器）
     */
    public boolean isFunctional() {
        return this == REQUEST_ID ||
               this == CORS ||
               this == REQUEST_LOG ||
               this == AUTH ||
               this == METRICS ||
               this == REQUEST_SIZE_LIMIT ||
               this == RATE_LIMIT ||
               this == DEFAULT_RESPONSE;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}