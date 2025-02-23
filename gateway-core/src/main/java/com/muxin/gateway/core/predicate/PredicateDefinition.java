package com.muxin.gateway.core.predicate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * 定义了一个谓词定义类，用于存储谓词的名称和参数。
 * 该类提供了从字符串解析谓词定义的功能，并支持通过名称和参数进行谓词配置。
 *
 * @author Administrator
 * @date 2024/11/21 10:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PredicateDefinition {

    /**
     * 谓词的名称。
     */
    private String name;

    /**
     * 谓词的参数映射，使用 LinkedHashMap 保持插入顺序。
     */
    private Map<String, String> args = new LinkedHashMap<>();

    /**
     * 无参构造函数。
     */
    public PredicateDefinition() {
    }

    /**
     * 从字符串构造谓词定义。
     * 字符串格式为 "name=value1,value2"，其中 value1 和 value2 是参数。
     *
     * @param text 谓词定义的字符串
     * @throws BeanDefinitionValidationException 如果字符串格式不正确
     */
    public PredicateDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            throw new BeanDefinitionValidationException(
                    "Unable to parse PredicateDefinition text '" + text + "'" + ", must be of the form name=value");
        }
        setName(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

        for (String arg : args) {
            this.args.put(UUID.randomUUID().toString(), arg);
        }
    }
}
