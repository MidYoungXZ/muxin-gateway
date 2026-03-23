package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("sys_config")
public class SysConfig {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private String configKey;
    
    private String configValue;
    
    private String configName;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
    
    @Column(isLogicDelete = true)
    private Integer deleted;
}