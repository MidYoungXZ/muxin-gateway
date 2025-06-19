package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 更新过滤器DTO
 *
 * @author muxin
 */
@Data
public class FilterUpdateDTO {
    
    /**
     * 过滤器名称
     */
    @NotBlank(message = "过滤器名称不能为空")
    private String filterName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 配置
     */
    private Map<String, Object> config;
    
    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer order;
} 