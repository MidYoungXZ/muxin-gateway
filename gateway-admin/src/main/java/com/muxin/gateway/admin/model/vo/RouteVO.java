package com.muxin.gateway.admin.model.vo;

import com.muxin.gateway.admin.entity.GwRoute.GrayscaleConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RouteVO {
    
    private Long id;
    
    private String routeId;
    
    private String routeName;
    
    private String description;
    
    private String uri;
    
    private List<PredicateInfo> predicates;
    
    private List<PluginVO> plugins;
    
    private Map<String, Object> metadata;
    
    private Integer order;
    
    private String loadBalanceStrategy;
    
    private Boolean enabled;
    
    private Boolean grayscaleEnabled;
    
    private GrayscaleConfig grayscaleConfig;
    
    private Long templateId;
    
    private String templateName;
    
    private Integer version;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
    
    @Data
    public static class PredicateInfo {
        private String predicateType;
        private Map<String, Object> args;
    }
}