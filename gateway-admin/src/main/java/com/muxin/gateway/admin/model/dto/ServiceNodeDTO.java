package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServiceNodeDTO {
    
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
}