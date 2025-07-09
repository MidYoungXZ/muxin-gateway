package com.muxin.gateway.core.plus.route.filter;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

/**
 * HTTP认证过滤器工厂
 * 
 * @author muxin
 */
@Slf4j
public class HttpAuthFilterFactory implements FilterFactory {
    
    @Override
    public String getSupportedFilterName() {
        return "AUTH";
    }
    
    @Override
    public void validateConfig(FilterDefinition definition) {
        if (definition.getConfig() == null) {
            throw new IllegalArgumentException("AUTH过滤器必须提供配置参数");
        }
        
        String authType = definition.getStringConfig("authType");
        if (authType == null || authType.trim().isEmpty()) {
            throw new IllegalArgumentException("AUTH过滤器必须指定authType");
        }
        
        // 验证支持的认证类型
        if (!Arrays.asList("JWT", "BASIC", "TOKEN").contains(authType.toUpperCase())) {
            throw new IllegalArgumentException("不支持的认证类型: " + authType);
        }
        
        // 根据认证类型验证特定参数
        switch (authType.toUpperCase()) {
            case "JWT" -> {
                if (definition.getStringConfig("secretKey") == null) {
                    throw new IllegalArgumentException("JWT认证必须提供secretKey");
                }
            }
            case "TOKEN" -> {
                if (definition.getStringConfig("tokenParam") == null) {
                    throw new IllegalArgumentException("TOKEN认证必须提供tokenParam");
                }
            }
            // BASIC认证通常不需要额外配置
        }
    }
    
    @Override
    public Filter createFilter(FilterDefinition definition) {
        // 每次调用都创建新实例，与特定路由绑定
        return HttpAuthFilter.builder()
            .name(getSupportedFilterName())
            .order(definition.getOrder())
            .enabled(definition.isEnabled())
            .authType(definition.getStringConfig("authType"))
            .secretKey(definition.getStringConfig("secretKey"))
            .tokenParam(definition.getStringConfig("tokenParam"))
            .build();
    }
} 