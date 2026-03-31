package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 路由插件配置DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RoutePluginDTO {
    
    private Long pluginId;
    
    private String pluginName;
    
    private String pluginType;
    
    private Map<String, Object> config;
    
    private Integer priorityOverride;
    
    private Boolean enabled;
}