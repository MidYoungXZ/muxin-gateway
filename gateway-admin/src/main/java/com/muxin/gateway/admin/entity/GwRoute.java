package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Table("gw_route")
public class GwRoute {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private String routeId;
    
    private String routeName;
    
    private String description;
    
    private String uri;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private Map<String, Object> metadata;
    
    @Column("order")
    private Integer order;
    
    private String loadBalanceStrategy;
    
    private Boolean enabled;
    
    private Boolean grayscaleEnabled;
    
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private GrayscaleConfig grayscaleConfig;
    
    private Long templateId;
    
    private Integer version;
    
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
    
    @RelationManyToMany(
            joinTable = "gw_route_predicate",
            selfField = "id",
            joinSelfColumn = "route_id",
            targetField = "id",
            joinTargetColumn = "predicate_id"
    )
    private List<GwPredicate> predicates;
    
    @Data
    public static class GrayscaleConfig {
        private String type;
        private Map<String, Object> config;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean autoPromote;
        private Boolean rollbackOnError;
    }
} 