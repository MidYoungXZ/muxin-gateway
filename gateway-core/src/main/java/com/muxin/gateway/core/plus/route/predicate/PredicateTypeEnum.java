package com.muxin.gateway.core.plus.route.predicate;

/**
 * 断言类型枚举
 * 定义网关支持的路由匹配断言类型
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PredicateTypeEnum {
    
    /**
     * 路径匹配断言
     */
    PATH("PATH", "路径匹配"),
    
    /**
     * HTTP方法断言
     */
    METHOD("METHOD", "HTTP方法匹配"),
    
    /**
     * 请求头断言
     */
    HEADER("HEADER", "请求头匹配"),
    
    /**
     * 查询参数断言
     */
    QUERY("QUERY", "查询参数匹配"),
    
    /**
     * IP地址断言
     */
    IP("IP", "IP地址匹配");
    
    private final String code;
    private final String description;
    
    PredicateTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取断言类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取断言类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取断言类型
     */
    public static PredicateTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (PredicateTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("不支持的断言类型: " + code);
    }
    
    /**
     * 是否为路径断言
     */
    public boolean isPath() {
        return this == PATH;
    }
    
    /**
     * 是否为方法断言
     */
    public boolean isMethod() {
        return this == METHOD;
    }
    
    /**
     * 是否为请求头断言
     */
    public boolean isHeader() {
        return this == HEADER;
    }
    
    /**
     * 是否为查询参数断言
     */
    public boolean isQuery() {
        return this == QUERY;
    }
    
    /**
     * 是否为IP地址断言
     */
    public boolean isIp() {
        return this == IP;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}