package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 路由测试DTO
 *
 * @author muxin
 */
@Data
public class RouteTestDTO {
    
    private String method;
    
    private String path;
    
    private Map<String, String> headers;
    
    private String body;
} 