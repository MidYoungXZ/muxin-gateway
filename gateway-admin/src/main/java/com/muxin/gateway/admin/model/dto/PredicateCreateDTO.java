package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 断言创建DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class PredicateCreateDTO {
    
    @NotBlank(message = "断言名称不能为空")
    private String predicateName;
    
    @NotBlank(message = "断言类型不能为空")
    private String predicateType;
    
    private String description;
    
    @NotNull(message = "断言参数不能为空")
    private Map<String, Object> args;
    
    private Boolean enabled = true;
} 