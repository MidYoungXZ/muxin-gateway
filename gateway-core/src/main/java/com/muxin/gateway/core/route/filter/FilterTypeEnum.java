package com.muxin.gateway.core.route.filter;

/**
 * 过滤器类型枚举
 * 
 * 定义过滤器的两种类型：PART（部分）和GLOBAL（全局）
 *
 * @author Administrator
 * @since 1.0.0
 */
public enum FilterTypeEnum {

    PART("part"),

    GLOBAL("global");

    private final String shortName;

    private FilterTypeEnum(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }
}
