package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 断言配置实体
 *
 * @author muxin
 */
@Data
@Table("gw_predicate")
public class GwPredicate {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private String predicateName;
    
    private String predicateType;
    
    private String description;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    private Boolean isSystem;
    
    private Boolean enabled;
    
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
} 