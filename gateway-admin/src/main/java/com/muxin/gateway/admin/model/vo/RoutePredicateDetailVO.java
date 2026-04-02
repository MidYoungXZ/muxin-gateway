package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RoutePredicateDetailVO {
    
    private Long id;
    
    private String predicateName;
    
    private String predicateType;
    
    private Map<String, Object> args;
    
    private Integer sortOrder;
}