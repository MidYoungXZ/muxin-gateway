package com.muxin.gateway.admin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 负载均衡查询DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoadBalanceQueryDTO extends PageQuery {
    
    private Long routeId;
    
    private String strategy;
    
    private Boolean enabled;
}