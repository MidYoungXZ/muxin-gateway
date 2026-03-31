package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.Map;

/**
 * 路由插件VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class PluginVO {
    
    private Long id;
    
    private Long pluginId;
    
    private String pluginName;
    
    private String pluginType;
    
    private Map<String, Object> config;
    
    private Integer priorityOverride;
    
    private Integer defaultPriority;
    
    private Integer effectivePriority;
    
    private Boolean enabled;
    
    private String phase;
}