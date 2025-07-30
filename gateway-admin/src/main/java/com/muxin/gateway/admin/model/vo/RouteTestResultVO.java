package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 路由测试结果VO
 *
 * @author muxin
 */
@Data
@Builder
public class RouteTestResultVO {
    
    /**
     * 是否匹配到路由
     */
    private Boolean matched;
    
    /**
     * 匹配的路由ID
     */
    private String matchedRouteId;
    
    /**
     * 匹配的断言列表
     */
    private List<String> matchedPredicates;
    
    /**
     * 应用的过滤器列表
     */
    private List<String> appliedFilters;
    
    /**
     * 目标URI
     */
    private String targetUri;
    
    /**
     * 响应状态码
     */
    private Integer statusCode;
    
    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;
    
    /**
     * 响应体
     */
    private String responseBody;
    
    /**
     * 耗时(ms)
     */
    private Long elapsedTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
} 