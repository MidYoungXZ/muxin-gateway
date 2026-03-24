package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 服务节点更新DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class ServiceNodeUpdateDTO {
    
    @NotBlank(message = "节点名称不能为空")
    @Size(max = 100, message = "节点名称长度不能超过100")
    private String nodeName;
    
    @NotBlank(message = "节点地址不能为空")
    @Size(max = 200, message = "节点地址长度不能超过200")
    private String address;
    
    @Min(value = 1, message = "端口范围1-65535")
    @Max(value = 65535, message = "端口范围1-65535")
    private Integer port;
    
    @Min(value = 1, message = "权重范围1-100")
    @Max(value = 100, message = "权重范围1-100")
    private Integer weight;
    
    private Integer maxFails;
    
    private Integer failTimeout;
    
    private Boolean backup;
    
    private Boolean healthCheckEnabled;
    
    private Integer healthCheckInterval;
    
    private Integer healthCheckTimeout;
    
    private String healthCheckPath;
    
    private List<Integer> healthCheckExpectedStatus;
}