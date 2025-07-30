package com.muxin.gateway.core.route.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:21
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
