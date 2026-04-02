package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RoutePluginDetailVO {
    
    private Long routePluginId;
    
    private Long pluginId;
    
    private String pluginName;
    
    private String pluginType;
    
    private Map<String, Object> config;
    
    private Map<String, Object> defaultConfig;
    
    private Integer priorityOverride;
    
    private Integer defaultPriority;
    
    private Boolean enabled;
    
    private String phase;
}