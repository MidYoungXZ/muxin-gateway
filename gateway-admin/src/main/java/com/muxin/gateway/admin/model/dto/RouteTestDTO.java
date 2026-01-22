package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 路由测试DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@Data
public class RouteTestDTO {
    
    private String method;
    
    private String path;
    
    private Map<String, String> headers;
    
    private String body;
} 