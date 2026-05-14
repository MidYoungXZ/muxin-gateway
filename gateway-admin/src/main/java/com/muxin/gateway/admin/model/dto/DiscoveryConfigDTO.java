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
    
    /**
     * 注册中心中的服务发现名称
     * 用于在注册中心（如Nacos）中查找服务实例时使用的名称
     * 留空则使用serviceName作为查询名称
     */
    private String discoveryServiceName;
}