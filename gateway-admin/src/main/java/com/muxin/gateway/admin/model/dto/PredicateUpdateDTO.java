package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 断言更新DTO
 *
 * @author muxin
 */
@Data
public class PredicateUpdateDTO {
    
    @NotBlank(message = "断言名称不能为空")
    private String predicateName;
    
    private String description;
    
    @NotNull(message = "断言配置不能为空")
    private Map<String, Object> config;
    
    private Boolean enabled;
}