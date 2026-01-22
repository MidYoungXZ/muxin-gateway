package com.muxin.gateway.core.plus.route;

/**
 * HTTP方法枚举
 * 定义网关支持的HTTP请求方法
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum HttpMethod {
    
    /**
     * GET请求 - 获取资源
     */
    GET("GET", "获取资源"),
    
    /**
     * POST请求 - 创建资源
     */
    POST("POST", "创建资源"),
    
    /**
     * PUT请求 - 更新资源
     */
    PUT("PUT", "更新资源"),
    
    /**
     * DELETE请求 - 删除资源
     */
    DELETE("DELETE", "删除资源"),
    
    /**
     * PATCH请求 - 部分更新资源
     */
    PATCH("PATCH", "部分更新资源"),
    
    /**
     * HEAD请求 - 获取资源头信息
     */
    HEAD("HEAD", "获取资源头信息"),
    
    /**
     * OPTIONS请求 - 获取支持的请求方法
     */
    OPTIONS("OPTIONS", "获取支持的请求方法");
    
    private final String code;
    private final String description;
    
    HttpMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取HTTP方法代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取HTTP方法描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取HTTP方法
     */
    public static HttpMethod fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (HttpMethod method : values()) {
            if (method.code.equalsIgnoreCase(code)) {
                return method;
            }
        }
        
        throw new IllegalArgumentException("不支持的HTTP方法: " + code);
    }
    
    /**
     * 是否为安全方法（不修改服务器状态）
     */
    public boolean isSafe() {
        return this == GET || this == HEAD || this == OPTIONS;
    }
    
    /**
     * 是否为幂等方法
     */
    public boolean isIdempotent() {
        return this == GET || this == HEAD || this == PUT || this == DELETE || this == OPTIONS;
    }
    
    /**
     * 是否需要请求体
     */
    public boolean requiresBody() {
        return this == POST || this == PUT || this == PATCH;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}