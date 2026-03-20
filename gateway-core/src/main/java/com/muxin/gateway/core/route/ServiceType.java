package com.muxin.gateway.core.route;

/**
 * 服务类型枚举
 * 定义网关支持的服务类型
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public enum ServiceType {

    /**
     * STATIC - 静态配置类型
     * 使用配置文件中定义的静态地址列表
     */
    STATIC("STATIC", "静态配置服务"),

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

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ServiceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ServiceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        if ("CONFIG".equalsIgnoreCase(code)) {
            return STATIC;
        }
        throw new IllegalArgumentException("不支持的服务类型: " + code);
    }

    public boolean isStatic() {
        return this == STATIC;
    }

    public boolean isDiscovery() {
        return this == DISCOVERY;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", code, description);
    }
}