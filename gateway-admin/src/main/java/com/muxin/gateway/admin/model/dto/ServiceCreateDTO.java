package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 服务创建DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class ServiceCreateDTO {
    
    @NotBlank(message = "服务名称不能为空")
    @Size(max = 100, message = "服务名称长度不能超过100")
    private String serviceName;
    
    private String nodeName;
    
    private String address;
    
    private Integer port;
}