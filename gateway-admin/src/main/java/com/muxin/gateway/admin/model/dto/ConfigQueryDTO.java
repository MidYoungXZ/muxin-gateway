package com.muxin.gateway.admin.model.dto;

import lombok.Data;

@Data
public class ConfigQueryDTO {
    
    private String configKey;
    
    private String configName;
    
    private Integer status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 20;
}