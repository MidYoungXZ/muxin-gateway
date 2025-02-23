package com.muxin.gateway.core.filter;

/**
 * 定义了一个枚举类型，表示过滤器的类型。该枚举类型包括请求过滤器、端点过滤器和响应过滤器。
 *
 * @author Administrator
 * @date 2024/11/20 16:21
 */
public enum FilterTypeEnum {

    /**
     * 请求过滤器类型。
     */
    REQUEST("request"),

    /**
     * 端点过滤器类型。
     */
    ENDPOINT("endpoint"),

    /**
     * 响应过滤器类型。
     */
    RESPONSE("response");

    private final String shortName;

    /**
     * 构造函数，用于设置过滤器类型的短名称。
     *
     * @param shortName 过滤器类型的短名称
     */
    private FilterTypeEnum(String shortName) {
        this.shortName = shortName;
    }

    /**
     * 返回过滤器类型的短名称。
     *
     * @return 过滤器类型的短名称
     */
    @Override
    public String toString() {
        return shortName;
    }

    /**
     * 根据字符串解析过滤器类型。
     *
     * @param str 过滤器类型的字符串表示
     * @return 对应的 {@link FilterTypeEnum} 枚举值
     * @throws IllegalArgumentException 如果字符串不是有效的过滤器类型
     */
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
