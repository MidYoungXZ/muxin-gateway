package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * 路由更新DTO
 *
 * @author muxin
 */
@Data
public class RouteUpdateDTO {
    
    @NotBlank(message = "路由名称不能为空")
    private String routeName;
    
    private String description;
    
    @NotBlank(message = "目标URI不能为空")
    private String uri;
    
    @NotEmpty(message = "至少需要配置一个断言")
    private List<Long> predicateIds;  // 断言ID列表
    
    private List<Long> filterIds;     // 过滤器ID列表
    
    private Map<String, Object> metadata;
    
    @Min(0)
    private Integer order;
    
    private Boolean enabled;
    
    private Boolean grayscaleEnabled;
    
    private Map<String, Object> grayscaleConfig;
} 