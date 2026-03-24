package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 服务统计VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class ServiceStatsVO {
    
    private String serviceName;
    
    private Integer totalNodes;
    
    private Integer healthyNodes;
    
    private Integer unhealthyNodes;
    
    private Integer enabledNodes;
    
    private Integer disabledNodes;
    
    private Integer maintenanceNodes;
}