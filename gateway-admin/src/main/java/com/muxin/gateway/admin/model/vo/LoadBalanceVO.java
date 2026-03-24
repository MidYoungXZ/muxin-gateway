package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 负载均衡VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class LoadBalanceVO {
    
    private Long id;
    
    private Long routeId;
    
    private String routeName;
    
    private String strategy;
    
    private String strategyDesc;
    
    private Map<String, Object> config;
    
    private Boolean enabled;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}