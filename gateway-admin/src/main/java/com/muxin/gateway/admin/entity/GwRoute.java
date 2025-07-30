package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 路由配置实体
 *
 * @author muxin
 */
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
    
    // 关联的断言列表（非数据库字段）
    @RelationManyToMany(
            joinTable = "gw_route_predicate",
            selfField = "id",
            joinSelfColumn = "route_id",
            targetField = "id",
            joinTargetColumn = "predicate_id"
    )
    private List<GwPredicate> predicates;
    
    // 关联的过滤器列表（非数据库字段）
    @RelationManyToMany(
            joinTable = "gw_route_filter",
            selfField = "id",
            joinSelfColumn = "route_id",
            targetField = "id",
            joinTargetColumn = "filter_id"
    )
    private List<GwFilter> filters;
    
    /**
     * 灰度配置
     */
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