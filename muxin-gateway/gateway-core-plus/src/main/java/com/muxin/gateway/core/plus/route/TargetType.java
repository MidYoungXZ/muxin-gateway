package com.muxin.gateway.core.plus.route;

/**
 * 路由目标类型枚举
 *
 * @author muxin
 */
public enum TargetType {
    
    /**
     * 静态地址配置
     * 直接配置具体的服务地址列表
     */
    STATIC("STATIC", "静态地址配置"),
    
    /**
     * 服务发现配置
     * 通过注册中心发现服务地址
     */
    DISCOVERY("DISCOVERY", "服务发现配置");
    
    private final String code;
    private final String description;
    
    TargetType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据字符串获取目标类型
     */
    public static TargetType fromString(String type) {
        if (type == null) {
            return STATIC; // 默认为静态配置
        }
        
        for (TargetType targetType : values()) {
            if (targetType.getCode().equalsIgnoreCase(type)) {
                return targetType;
            }
        }
        
        throw new IllegalArgumentException("不支持的目标类型: " + type);
    }
    
    /**
     * 是否为静态配置类型
     */
    public boolean isStatic() {
        return this == STATIC;
    }
    
    /**
     * 是否为服务发现类型
     */
    public boolean isDiscovery() {
        return this == DISCOVERY;
    }
} 