package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigUpdateDTO {
    
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;
    
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    private Integer status;
}