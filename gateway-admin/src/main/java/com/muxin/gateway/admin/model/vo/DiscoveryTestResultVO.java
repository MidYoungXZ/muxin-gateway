package com.muxin.gateway.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryTestResultVO {
    
    private Boolean success;
    
    private String message;
    
    private List<String> serviceNames;
}