package com.muxin.gateway.core.plus.route.predicate;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Arrays;

/**
 * HTTP方法断言工厂
 * 
 * @author muxin
 */
@Slf4j
public class HttpMethodPredicateFactory implements PredicateFactory {
    
    private static final List<String> VALID_HTTP_METHODS = Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE", "CONNECT"
    );
    
    @Override
    public String getSupportedPredicateName() {
        return "METHOD";
    }
    
    @Override
    public void validateConfig(PredicateDefinition definition) {
        if (definition.getConfig() == null) {
            throw new IllegalArgumentException("METHOD断言必须提供配置参数");
        }
        
        Object methodsObj = definition.getConfig().get("methods");
        if (methodsObj == null) {
            throw new IllegalArgumentException("METHOD断言必须指定methods参数");
        }
        
        // 验证methods参数类型
        if (!(methodsObj instanceof List)) {
            throw new IllegalArgumentException("methods参数必须是数组类型");
        }
        
        @SuppressWarnings("unchecked")
        List<Object> methods = (List<Object>) methodsObj;
        
        if (methods.isEmpty()) {
            throw new IllegalArgumentException("methods参数不能为空");
        }
        
        // 验证每个HTTP方法是否有效
        for (Object methodObj : methods) {
            if (!(methodObj instanceof String)) {
                throw new IllegalArgumentException("HTTP方法必须是字符串类型");
            }
            
            String method = ((String) methodObj).toUpperCase();
            if (!VALID_HTTP_METHODS.contains(method)) {
                throw new IllegalArgumentException("不支持的HTTP方法: " + method + 
                    ", 支持的方法: " + String.join(", ", VALID_HTTP_METHODS));
            }
        }
    }
    
    @Override
    public Predicate createPredicate(PredicateDefinition definition) {
        @SuppressWarnings("unchecked")
        List<String> methods = (List<String>) definition.getConfig().get("methods");
        
        // 创建新的HttpMethodPredicate实例
        return new HttpMethodPredicate(methods);
    }
} 