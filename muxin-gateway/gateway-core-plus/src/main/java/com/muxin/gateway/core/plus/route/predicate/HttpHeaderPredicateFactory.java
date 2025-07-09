package com.muxin.gateway.core.plus.route.predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP头部断言工厂
 * 
 * @author muxin
 */
@Slf4j
public class HttpHeaderPredicateFactory implements PredicateFactory {
    
    @Override
    public String getSupportedPredicateName() {
        return "HEADER";
    }
    
    @Override
    public void validateConfig(PredicateDefinition definition) {
        if (definition.getConfig() == null) {
            throw new IllegalArgumentException("HEADER断言必须提供配置参数");
        }
        
        String name = definition.getStringConfig("name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("HEADER断言必须指定name参数");
        }
        
        String value = definition.getStringConfig("value");
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("HEADER断言必须指定value参数");
        }
        
        // 验证matchType参数
        String matchType = definition.getStringConfig("matchType", "EXACT");
        if (!java.util.Arrays.asList("EXACT", "REGEX", "CONTAINS", "STARTS_WITH", "ENDS_WITH")
                .contains(matchType.toUpperCase())) {
            throw new IllegalArgumentException("不支持的matchType: " + matchType + 
                ", 支持的类型: EXACT, REGEX, CONTAINS, STARTS_WITH, ENDS_WITH");
        }
        
        // 如果是正则表达式，验证正则语法
        if ("REGEX".equalsIgnoreCase(matchType)) {
            try {
                java.util.regex.Pattern.compile(value);
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new IllegalArgumentException("无效的正则表达式: " + value, e);
            }
        }
    }
    
    @Override
    public Predicate createPredicate(PredicateDefinition definition) {
        String name = definition.getStringConfig("name");
        String value = definition.getStringConfig("value");
        String matchType = definition.getStringConfig("matchType", "EXACT");
        
        // 创建新的HttpHeaderPredicate实例
        return new HttpHeaderPredicate(name, value, matchType);
    }
} 