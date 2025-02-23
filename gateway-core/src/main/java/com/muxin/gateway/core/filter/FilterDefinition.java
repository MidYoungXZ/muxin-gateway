package com.muxin.gateway.core.filter;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定义了一个过滤器定义类。该类用于存储过滤器的名称和参数。
 * 通过该类可以方便地配置和管理过滤器。
 *
 * @author Administrator
 * @date 2024/11/20 16:30
 */
@Data
public class FilterDefinition {

    /**
     * 过滤器的名称。
     */
    private String name;

    /**
     * 过滤器的参数映射，使用 LinkedHashMap 保持插入顺序。
     */
    private Map<String, String> args = new LinkedHashMap<>();

    /**
     * 无参构造函数。
     */
    public FilterDefinition() {}

    /**
     * 从字符串构造过滤器定义。
     * 字符串格式为 "name=value"，如果不存在等号，则整个字符串作为名称。
     *
     * @param text 过滤器定义的字符串
     */
    public FilterDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            setName(text);
            return;
        }
        setName(text.substring(0, eqIdx));
        if (eqIdx < text.length() - 1) {
            args.put("value", text.substring(eqIdx + 1));
        }
    }
}
