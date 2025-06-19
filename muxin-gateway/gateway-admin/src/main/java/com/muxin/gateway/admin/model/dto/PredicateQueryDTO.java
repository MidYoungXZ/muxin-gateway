package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 断言查询DTO
 *
 * @author muxin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PredicateQueryDTO extends PageQuery {
    
    private String predicateName;
    
    private String predicateType;
    
    private Boolean enabled;
    
    private Boolean isSystem;
} 