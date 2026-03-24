package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 负载均衡更新DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class LoadBalanceUpdateDTO {
    
    @NotNull(message = "策略不能为空")
    private String strategy;
    
    private Map<String, Object> config;
    
    private Boolean enabled;
}