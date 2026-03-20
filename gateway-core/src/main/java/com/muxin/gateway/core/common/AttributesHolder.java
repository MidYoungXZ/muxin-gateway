package com.muxin.gateway.core.common;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 属性持有者接口
 * 提供统一的属性存储和管理功能，支持泛型类型安全的属性访问
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AttributesHolder extends Serializable {
    /**
     * 获取所有属性
     * @return
     */
    Map<String, Object> getAttributes();

    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String name) {
        return (T) getAttributes().get(name);
    }

    default <T> T getRequiredAttribute(String name) {
        T value = getAttribute(name);
        Objects.requireNonNull(value, "Required attribute '" + name + "' is missing");
        return value;
    }

    @SuppressWarnings("unchecked")
    default <T> T getAttributeOrDefault(String name, T defaultValue) {
        return (T) getAttributes().getOrDefault(name, defaultValue);
    }

    /**
     * 设置属性
     * @param name 属性名
     * @param value 属性值
     */
    default void setAttribute(String name, Object value) {
        if (value != null) {
            getAttributes().put(name, value);
        } else {
            getAttributes().remove(name);
        }
    }

    /**
     * 检查是否包含属性
     * @param name 属性名
     * @return 是否包含
     */
    default boolean hasAttribute(String name) {
        return getAttributes().containsKey(name);
    }

    /**
     * 移除属性
     * @param name 属性名
     */
    default void removeAttribute(String name) {
        getAttributes().remove(name);
    }
}
