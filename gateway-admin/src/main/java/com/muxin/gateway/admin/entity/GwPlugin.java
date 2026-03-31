package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 插件模板实体
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Table("gw_plugin")
public class GwPlugin {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private String pluginName;
    
    private String pluginType;
    
    private String description;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> schema;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> defaultConfig;
    
    private Integer defaultPriority;
    
    private String phase;
    
    private String icon;
    
    private Boolean isSystem;
    
    private Boolean enabled;
    
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
}