package com.muxin.gateway.core.plus.utils;

/**
 * JSON工具类 - 简单实现
 *
 * @author muxin
 */
public class JsonUtils {
    
    /**
     * 对象转JSON字符串
     * 简单实现，后续可以集成Jackson
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        
        if (object instanceof String) {
            return "\"" + object + "\"";
        }
        
        if (object instanceof Number || object instanceof Boolean) {
            return object.toString();
        }
        
        // 对于其他类型，返回toString结果
        return "\"" + object.toString() + "\"";
    }
    
    /**
     * JSON字符串转对象
     * 简单实现，后续可以集成Jackson
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty() || "null".equals(json)) {
            return null;
        }
        
        // 简单类型处理
        if (clazz == String.class) {
            if (json.startsWith("\"") && json.endsWith("\"")) {
                return clazz.cast(json.substring(1, json.length() - 1));
            }
            return clazz.cast(json);
        }
        
        if (clazz == Integer.class) {
            return clazz.cast(Integer.valueOf(json));
        }
        
        if (clazz == Long.class) {
            return clazz.cast(Long.valueOf(json));
        }
        
        if (clazz == Boolean.class) {
            return clazz.cast(Boolean.valueOf(json));
        }
        
        // 对于复杂类型，抛出异常提示需要更完善的实现
        throw new UnsupportedOperationException("当前JSON工具类不支持复杂类型转换: " + clazz.getName());
    }
    
    /**
     * 对象转JSON字节数组
     */
    public static byte[] toJsonBytes(Object object) {
        String json = toJson(object);
        return json.getBytes();
    }
    
    /**
     * JSON字节数组转对象
     */
    public static <T> T fromJsonBytes(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        String json = new String(jsonBytes);
        return fromJson(json, clazz);
    }
} 