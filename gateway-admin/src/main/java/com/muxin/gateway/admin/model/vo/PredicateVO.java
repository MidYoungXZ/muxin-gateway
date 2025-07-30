package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 断言VO
 *
 * @author muxin
 */
@Data
public class PredicateVO {
    
    private Long id;
    
    private String predicateName;
    
    private String predicateType;
    
    private String predicateTypeDesc;  // 类型描述
    
    private String description;
    
    private Map<String, Object> config;
    
    private Boolean isSystem;
    
    private Boolean enabled;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Integer usageCount;  // 使用次数
} 