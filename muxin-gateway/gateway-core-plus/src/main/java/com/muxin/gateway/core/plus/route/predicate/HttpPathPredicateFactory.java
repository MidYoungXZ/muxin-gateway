package com.muxin.gateway.core.plus.route.predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP路径断言工厂
 * 
 * @author muxin
 */
@Slf4j
public class HttpPathPredicateFactory implements PredicateFactory {
    
    @Override
    public String getSupportedPredicateName() {
        return "PATH";
    }
    
    @Override
    public void validateConfig(PredicateDefinition definition) {
        if (definition.getConfig() == null) {
            throw new IllegalArgumentException("PATH断言必须提供配置参数");
        }
        
        String pattern = definition.getStringConfig("pattern");
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("PATH断言必须指定pattern参数");
        }
        
        // 验证stripPrefix参数
        Object stripPrefix = definition.getConfig().get("stripPrefix");
        if (stripPrefix != null && !(stripPrefix instanceof Number)) {
            throw new IllegalArgumentException("stripPrefix必须是数字类型");
        }
        
        // 验证isRegex参数
        Object isRegex = definition.getConfig().get("isRegex");
        if (isRegex != null && !(isRegex instanceof Boolean)) {
            throw new IllegalArgumentException("isRegex必须是布尔类型");
        }
        
        // 如果是正则表达式，验证正则语法
        boolean regex = definition.getConfigValue("isRegex", false);
        if (regex) {
            try {
                java.util.regex.Pattern.compile(pattern);
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new IllegalArgumentException("无效的正则表达式: " + pattern, e);
            }
        }
    }
    
    @Override
    public Predicate createPredicate(PredicateDefinition definition) {
        String pattern = definition.getStringConfig("pattern");
        boolean isRegex = definition.getConfigValue("isRegex", false);
        
        // 创建新的HttpPathPredicate实例
        return new HttpPathPredicate(pattern, isRegex);
    }
} 