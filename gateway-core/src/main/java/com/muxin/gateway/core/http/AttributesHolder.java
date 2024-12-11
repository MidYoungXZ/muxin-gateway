package com.muxin.gateway.core.http;

import java.util.Map;
import java.util.Objects;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/18 16:40
 */
public interface AttributesHolder {
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

}
