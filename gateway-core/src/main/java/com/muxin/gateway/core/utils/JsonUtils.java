package com.muxin.gateway.core.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/13 11:35
 */
@Getter
public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 配置序列化和反序列化的特性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 待转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param json  JSON 字符串
     * @param clazz 对象的类型
     * @param <T>   类型参数
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON string to object", e);
        }
    }

    /**
     * 将 JSON 字符串转换为指定的泛型类型对象
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用
     * @param <T>           类型参数
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON string to object with type reference", e);
        }
    }

    /**
     * 将对象转换为 JSON 字符串并格式化输出
     *
     * @param object 待转换的对象
     * @return 格式化后的 JSON 字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to pretty JSON string", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 List
     *
     * @param json  JSON 字符串
     * @param clazz List 中元素的类型
     * @param <T>   类型参数
     * @return 转换后的 List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON string to List", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param json JSON 字符串
     * @param <K>  Map 的键类型
     * @param <V>  Map 的值类型
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON string to Map", e);
        }
    }

}
