package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * 路由更新DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RouteUpdateDTO {
    
    @NotBlank(message = "路由名称不能为空")
    private String routeName;
    
    private String description;
    
    @NotBlank(message = "目标URI不能为空")
    private String uri;
    
    private RouteMatchingDTO matching;
    
    private List<RoutePluginDTO> plugins;
    
    private String loadBalanceStrategy;
    
    private RouteCreateDTO.PathRewriteDTO pathRewrite;
    
    private RouteCreateDTO.TimeoutDTO timeouts;
    
    private Map<String, Object> metadata;
    
    @Min(0)
    private Integer order;
    
    private Boolean enabled;
    
    private Boolean grayscaleEnabled;
    
    private Map<String, Object> grayscaleConfig;
}