package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigVO {
    
    private Long id;
    
    private String configKey;
    
    private String configValue;
    
    private String configName;
    
    private String description;
    
    private Integer status;
    
    private String statusText;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}