package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class ServiceCreateDTO {
    
    public static final String MODE_MANUAL = "MANUAL";
    public static final String MODE_DISCOVERY = "DISCOVERY";
    
    @NotBlank(message = "服务名称不能为空")
    @Size(max = 100, message = "服务名称长度不能超过100")
    private String serviceName;
    
    @NotBlank(message = "创建模式不能为空")
    private String createMode;
    
    private List<ServiceNodeDTO> nodes;
    
    private DiscoveryConfigDTO discoveryConfig;
}