package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class DiscoveryConfigDTO {
    
    @NotBlank(message = "注册中心类型不能为空")
    private String registryType;
    
    @NotBlank(message = "注册中心地址不能为空")
    private String serverAddr;
    
    private String namespace;
    
    private String username;
    
    private String password;
    
    private String group;
}