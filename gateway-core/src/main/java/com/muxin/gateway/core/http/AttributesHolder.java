package com.muxin.gateway.core.http;

import java.util.Map;
import java.util.Objects;

/**
 * AttributesHolder 接口用于管理 HTTP 请求或响应中的属性。
 * 它提供了一组方法来获取、检查和操作这些属性。
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface AttributesHolder {

    /**
     * 获取所有属性的映射表。
     *
     * @return 包含所有属性的 Map，键为属性名，值为属性值
     */
    Map<String, Object> getAttributes();

    /**
     * 根据属性名获取指定的属性值。
     * 如果属性不存在，则返回 null。
     *
     * @param name 属性名
     * @param <T>  属性值的类型
     * @return 属性值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String name) {
        return (T) getAttributes().get(name);
    }

    /**
     * 根据属性名获取指定的属性值，并确保该属性存在。
     * 如果属性不存在，则抛出 NullPointerException。
     *
     * @param name 属性名
     * @param <T>  属性值的类型
     * @return 属性值
     * @throws NullPointerException 如果属性不存在
     */
    default <T> T getRequiredAttribute(String name) {
        T value = getAttribute(name);
        Objects.requireNonNull(value, "Required attribute '" + name + "' is missing");
        return value;
    }

    /**
     * 根据属性名获取指定的属性值。
     * 如果属性不存在，则返回默认值。
     *
     * @param name         属性名
     * @param defaultValue 默认值
     * @param <T>          属性值的类型
     * @return 属性值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    default <T> T getAttributeOrDefault(String name, T defaultValue) {
        return (T) getAttributes().getOrDefault(name, defaultValue);
    }
}
