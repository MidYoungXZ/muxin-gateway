package com.muxin.gateway.core.plus.route;

/**
 * 服务类型枚举
 * 定义网关支持的服务类型
 *
 * @author muxin
 */
public enum ServiceType {
    
    /**
     * CONFIG - 静态配置类型
     * 使用配置文件中定义的静态地址列表
     */
    CONFIG("CONFIG", "静态配置服务"),
    
    /**
     * DISCOVERY - 服务发现类型  
     * 通过注册中心动态发现服务实例
     */
    DISCOVERY("DISCOVERY", "服务发现服务");
    
    private final String code;
    private final String description;
    
    ServiceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取服务类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取服务类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取服务类型
     */
    public static ServiceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (ServiceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("不支持的服务类型: " + code);
    }
    
    /**
     * 是否为静态配置类型
     */
    public boolean isConfig() {
        return this == CONFIG;
    }
    
    /**
     * 是否为服务发现类型
     */
    public boolean isDiscovery() {
        return this == DISCOVERY;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
} 