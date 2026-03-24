package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务节点VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class ServiceNodeVO {
    
    private Long id;
    
    private String nodeId;
    
    private String serviceName;
    
    private String nodeName;
    
    private String address;
    
    private Integer port;
    
    private Integer weight;
    
    private Integer maxFails;
    
    private Integer failTimeout;
    
    private Boolean backup;
    
    private Boolean healthCheckEnabled;
    
    private Integer healthCheckInterval;
    
    private Integer healthCheckTimeout;
    
    private String healthCheckPath;
    
    private List<Integer> healthCheckExpectedStatus;
    
    private Integer status;
    
    private String statusDesc;
    
    private Boolean healthy;
    
    private LocalDateTime lastCheckTime;
    
    private Integer lastCheckResult;
}