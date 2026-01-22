package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 路由查询DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RouteQueryDTO extends PageQuery {
    
    private String routeName;
    
    private String uri;
    
    private Boolean enabled;
    
    private Boolean grayscaleEnabled;
    
    private Long templateId;
} 