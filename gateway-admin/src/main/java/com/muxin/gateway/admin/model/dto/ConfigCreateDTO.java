package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigCreateDTO {
    
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;
    
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;
    
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    private Integer status = 1;
}