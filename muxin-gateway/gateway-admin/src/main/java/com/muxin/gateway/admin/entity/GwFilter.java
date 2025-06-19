package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 过滤器配置实体
 *
 * @author muxin
 */
@Data
@Table("gw_filter")
public class GwFilter {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private String filterName;
    
    private String filterType;
    
    private String description;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    @Column("order")
    private Integer order;
    
    private Boolean isSystem;
    
    private Boolean enabled;
    
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
} 