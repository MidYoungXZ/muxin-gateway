package com.muxin.gateway.core.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:21
 */
public enum FilterTypeEnum {

    REQUEST("request"),
    ENDPOINT("endpoint"),
    RESPONSE("response");

    private final String shortName;

    private FilterTypeEnum(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    public static FilterTypeEnum parse(String str) {
        str = str.toLowerCase();
        switch (str) {
            case "request":
                return REQUEST;
            case "response":
                return RESPONSE;
            case "endpoint":
                return ENDPOINT;
            default:
                throw new IllegalArgumentException("Unknown filter type! type=" + String.valueOf(str));
        }
    }


}
