package com.muxin.gateway.core.route.filter;

/**
 * 过滤器执行阶段枚举
 * 定义过滤器在请求处理流程中的执行时机
 */
public enum FilterType {
    
    PRE("PRE", "前置过滤器-路由匹配后、后端调用前执行"),
    POST("POST", "后置过滤器-后端调用后、响应返回前执行");
    
    private final String code;
    private final String description;
    
    FilterType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static FilterType fromCode(String code) {
        if (code == null) return null;
        for (FilterType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        throw new IllegalArgumentException("不支持的过滤器类型: " + code);
    }
    
    public boolean isPre() {
        return this == PRE;
    }
    
    public boolean isPost() {
        return this == POST;
    }
}