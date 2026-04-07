package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * 路由创建DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RouteCreateDTO {
    
    @NotBlank(message = "路由标识不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "路由标识只能包含字母、数字、下划线和中划线")
    private String routeId;
    
    @NotBlank(message = "路由名称不能为空")
    private String routeName;
    
    private String description;
    
    @NotBlank(message = "目标URI不能为空")
    private String uri;
    
    private RouteMatchingDTO matching;
    
    private List<RoutePluginDTO> plugins;
    
    private String loadBalanceStrategy;
    
    private Map<String, Object> metadata;
    
    @Min(0)
    private Integer order;
    
    private Boolean enabled = true;
    
    private Long templateId;
}