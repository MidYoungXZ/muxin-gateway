package com.muxin.gateway.core.service;

/**
 * 实例来源枚举
 * 区分实例的获取方式
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public enum InstanceSource {

    /**
     * 静态配置来源
     * 从配置文件中定义的静态地址
     */
    STATIC("STATIC", "静态配置"),

    /**
     * 服务发现来源
     * 从注册中心动态发现
     */
    DISCOVERY("DISCOVERY", "服务发现");

    private final String code;
    private final String description;

    InstanceSource(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStatic() {
        return this == STATIC;
    }

    public boolean isDiscovery() {
        return this == DISCOVERY;
    }

    public static InstanceSource fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (InstanceSource source : values()) {
            if (source.code.equalsIgnoreCase(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("不支持的实例来源: " + code);
    }
}