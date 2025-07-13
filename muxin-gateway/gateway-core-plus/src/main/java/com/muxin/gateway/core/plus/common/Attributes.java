package com.muxin.gateway.core.plus.common;

import java.util.Map;

/**
 * @author muxin
 * @description:
 */
public interface Attributes {


    Map<String, Object> getAttributes();

    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttributes().get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("无法将属性 " + key + " 转换为类型 " + type.getName());
    }

    default void setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                getAttributes().put(key, value);
            } else {
                getAttributes().remove(key);
            }
        }
    }

}
