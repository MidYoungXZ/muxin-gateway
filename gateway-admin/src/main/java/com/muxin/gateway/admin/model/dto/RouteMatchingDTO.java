package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 路由匹配配置DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RouteMatchingDTO {
    
    private PathMatchingDTO path;
    
    private List<String> methods;
    
    private List<HeaderMatchingDTO> headers;
    
    private List<String> hosts;
    
    private List<QueryMatchingDTO> queries;
    
    @Data
    public static class PathMatchingDTO {
        private String pattern;
        private String matchType;
        private Boolean ignoreCase;
    }
    
    @Data
    public static class HeaderMatchingDTO {
        private String name;
        private String value;
        private String matchType;
    }
    
    @Data
    public static class QueryMatchingDTO {
        private String name;
        private String value;
        private String matchType;
    }
}