package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 路由插件关联实体
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Table("gw_route_plugin")
public class GwRoutePlugin {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private Long routeId;
    
    private Long pluginId;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    private Integer priorityOverride;
    
    private Boolean enabled;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}