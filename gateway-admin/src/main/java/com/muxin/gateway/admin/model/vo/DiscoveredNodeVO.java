package com.muxin.gateway.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredNodeVO {
    
    private String instanceId;
    
    private String address;
    
    private Integer port;
    
    private Integer weight;
    
    private Boolean healthy;
    
    private Boolean enabled;
    
    private Map<String, String> metadata;
}