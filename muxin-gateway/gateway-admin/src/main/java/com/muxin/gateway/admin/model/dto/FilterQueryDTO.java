package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 过滤器查询DTO
 *
 * @author muxin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FilterQueryDTO extends PageQueryDTO {
    
    /**
     * 过滤器名称
     */
    private String filterName;
    
    /**
     * 过滤器类型
     */
    private String filterType;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 是否系统内置
     */
    private Boolean isSystem;
} 