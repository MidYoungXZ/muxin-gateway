package com.muxin.gateway.core.plus.route.filter;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP日志过滤器工厂
 * 
 * @author muxin
 */
@Slf4j
public class HttpLoggingFilterFactory implements FilterFactory {
    
    @Override
    public String getSupportedFilterName() {
        return "REQUEST_LOG";
    }
    
    @Override
    public void validateConfig(FilterDefinition definition) {
        if (definition.getConfig() != null) {
            // 验证maxBodySize
            Object maxBodySize = definition.getConfig().get("maxBodySize");
            if (maxBodySize != null) {
                if (!(maxBodySize instanceof Number)) {
                    throw new IllegalArgumentException("maxBodySize必须是数字类型");
                }
                if (((Number) maxBodySize).intValue() < 0) {
                    throw new IllegalArgumentException("maxBodySize不能为负数");
                }
            }
            
            // 验证includeBody
            Object includeBody = definition.getConfig().get("includeBody");
            if (includeBody != null && !(includeBody instanceof Boolean)) {
                throw new IllegalArgumentException("includeBody必须是布尔类型");
            }
            
            // 验证includeHeaders
            Object includeHeaders = definition.getConfig().get("includeHeaders");
            if (includeHeaders != null && !(includeHeaders instanceof Boolean)) {
                throw new IllegalArgumentException("includeHeaders必须是布尔类型");
            }
        }
    }
    
    @Override
    public Filter createFilter(FilterDefinition definition) {
        // 每次调用都创建新实例，与特定路由绑定
        return HttpLoggingFilter.builder()
            .name(getSupportedFilterName())
            .order(definition.getOrder())
            .enabled(definition.isEnabled())
            .includeHeaders(definition.getConfigValue("includeHeaders", true))
            .includeBody(definition.getConfigValue("includeBody", false))
            .maxBodySize(definition.getConfigValue("maxBodySize", 1024))
            .build();
    }
} 